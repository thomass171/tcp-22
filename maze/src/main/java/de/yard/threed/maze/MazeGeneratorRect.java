package de.yard.threed.maze;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.IntHolder;
import de.yard.threed.core.Point;
import de.yard.threed.core.PointValidator;
import de.yard.threed.core.PointVisitor;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.util.DeterministicIntProvider;
import de.yard.threed.engine.util.IntProvider;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A different rectangle based layout with rooms/corridors like nethack.
 * (0,0) is lower left
 */
public class MazeGeneratorRect {
    int width, height;
    IntProvider intProvider;
    // every cell where there is no wall are fields
    List<Point> fields = new ArrayList<>();
    // just some additional meta data (redundant to 'fields') for easier handling.
    List<Room> rooms = new ArrayList<>();
    List<Corridor> corridors = new ArrayList<>();

    MazeGeneratorRect(int width, int height, IntProvider intProvider) {
        this.width = width;
        this.height = height;
        //intProvider = new RandomIntProvider((int) (System.currentTimeMillis() % 10000));
        this.intProvider = intProvider;

        // starting room on left side
        addRoom(2, 3, lowLeft -> lowLeft.getX() == 1);
        // target room on right side
        addRoom(2, 3, lowLeft -> lowLeft.getX() == width - 3);
    }

    /**
     * Place a new room randomly.
     */
    public boolean addRoom(int rw, int rh, PointValidator constraint) {
        List<Point> candidates = getCandidates();
        do {
            Point lowLeftCandidate = candidates.get(intProvider.nextInt() % candidates.size());
            BooleanHolder broken = new BooleanHolder();
            loop(rw, rh, o -> {
                if (!candidates.contains(lowLeftCandidate.add(o))) {
                    broken.setValue(true);
                }
            });
            if (!broken.getValue() && (constraint == null || constraint.isValid(lowLeftCandidate))) {
                // room found
                addRoom(lowLeftCandidate, rw, rh);
                return true;
            }
            candidates.remove(lowLeftCandidate);
        }
        while (candidates.size() > 0);
        return false;
    }

    public boolean addRoom(int rw, int rh) {
        return addRoom(rw, rh, null);
    }

    public boolean addRoom(Point lowLeft, int rw, int rh) {
        loop(rw, rh, o -> {
            fields.add(lowLeft.add(o));
        });
        rooms.add(new Room(lowLeft, rw, rh));
        return true;
    }

    public List<Point> getRoom() {
        //TODO
        return null;
    }

    public boolean addCorridor(int r0, int r1) {
        Room room0 = rooms.get(r0);
        Room room1 = rooms.get(r1);

        int distanceX = getDistanceX(room0, room1);
        int distanceY = getDistanceY(room0, room1);

        // where to leave r0?
        List<Point> leaveCandidates = new ArrayList<>();
        List<Point> enterCandidates = new ArrayList<>();
        Point turnPoint;
        List<CorridorCandidate> corridorCandidates = new ArrayList<CorridorCandidate>();

        if (distanceX >= 2) {
            // room1 right of room0, horizontal corridor. leave right
            leaveCandidates = getLeftRightDoorCandidates(room0, false);
            enterCandidates = getLeftRightDoorCandidates(room1, true);
            CorridorCandidate cc = new CorridorCandidate();

            if (distanceX == 2) {
                // too short for turn

                for (Point p1 : leaveCandidates) {
                    for (Point p2 : enterCandidates) {
                        if (p1.getY() == p2.getY()) {
                            cc.leave = p1;
                            cc.enter = p2;

                        }
                    }
                }
                if (cc.enter != null) {
                    cc.corridorSegments.add(new CorridorSegment(new Point(1, 0), cc.enter.getX() - cc.leave.getX() - 1));
                    corridorCandidates.add(cc);
                } else {
                    getLogger().debug("no short way");
                }
            } else {
                int steps0 = distanceX / 2;
                int meetX = leaveCandidates.get(0).getX() + steps0;

                cc.leave = pickRandomPointFromList(leaveCandidates);
                cc.enter = pickRandomPointFromList(enterCandidates);

                cc.corridorSegments.add(new CorridorSegment(new Point(1, 0), steps0));
                cc.corridorSegments.add(new CorridorSegment(new Point(0, cc.leave.getY() < cc.enter.getY() ? 1 : -1), Math.abs(cc.leave.getY() - cc.enter.getY())));
                // cannot dig into enter field because its no candidate
                cc.corridorSegments.add(new CorridorSegment(new Point(1, 0), cc.enter.getX() - meetX - 1));
                corridorCandidates.add(cc);
            }
        }
        if (distanceY >= 2) {
            // room1 above of room0, vertical corridor. leave on top
            leaveCandidates = getLowerUpperDoorCandidates(room0, false);
            enterCandidates = getLowerUpperDoorCandidates(room1, true);

            CorridorCandidate cc = new CorridorCandidate();

            cc.leave = pickRandomPointFromList(leaveCandidates);
            cc.enter = pickRandomPointFromList(enterCandidates);
            if (cc.leave != null && cc.enter != null) {
                if (cc.leave.getX() != cc.enter.getX()) {
                    int steps0 = distanceY / 2;
                    int turnY = leaveCandidates.get(0).getY() + steps0;

                    //turnPoint = new Point();
                    cc.corridorSegments.add(new CorridorSegment(new Point(0, 1), steps0));
                    cc.corridorSegments.add(new CorridorSegment(new Point(cc.leave.getX() < cc.enter.getX() ? 1 : -1, 0), Math.abs(cc.leave.getX() - cc.enter.getX())));
                    // cannot dig into enter field because its no candidate
                    cc.corridorSegments.add(new CorridorSegment(new Point(0, 1), cc.enter.getY() - turnY - 1));
                    corridorCandidates.add(cc);
                } else {
                    getLogger().debug("not yet");
                    return false;
                }
            }
        }
        for (CorridorCandidate cc : corridorCandidates) {
            List<Point> corridorFields = probeCorridorCandidate(cc);
            if (corridorFields != null) {
                fields.addAll(corridorFields);
                corridors.add(new Corridor(corridorFields));
                return true;
            }
        }
        getLogger().debug("no corridor of " + corridorCandidates.size() + " candidates succeeded");
        return false;
    }

    public List<Point> getCorridor(int corridor) {
        return corridors.get(corridor).corridorFields;
    }

    public static MazeGeneratorRect buildSampleV1() {
        MazeGeneratorRect layoutRect = new MazeGeneratorRect(80, 25,
                new DeterministicIntProvider(new int[]{1, 5, 276}));

        layoutRect.addRoom(new Point(8, 8), 5, 9);
        layoutRect.addCorridor(0, 2);
        layoutRect.addRoom(new Point(19, 2), 3, 5);
        layoutRect.addCorridor(2, 3);
        layoutRect.addRoom(new Point(20, 11), 4, 6);
        layoutRect.addCorridor(3, 4);
        layoutRect.addRoom(new Point(24, 1), 6, 3);
        layoutRect.addCorridor(3, 5);
        layoutRect.addRoom(new Point(42, 8), 5, 5);
        layoutRect.addCorridor(5, 6);
        layoutRect.addCorridor(6, 1);
        return layoutRect;
    }

    private List<Point> probeCorridorCandidate(CorridorCandidate cc) {
        List<Point> corridorFields = new ArrayList<Point>();
        corridorFields.add(cc.leave);
        Point pointer = cc.leave;
        for (CorridorSegment segment : cc.corridorSegments) {
            if ((pointer = digCorridor(pointer, segment, corridorFields)) == null) {
                return null;
            }
        }
        // cannot dig into enter field because its no candidate. Add it manually
        corridorFields.add(cc.enter);
        corridorFields = corridorFields.stream().distinct().collect(toList());
        return corridorFields;
    }

    private int getDistanceX(Room room0, Room room1) {
        if (room1.lowLeft.getX() > room0.lowLeft.getX() + room0.rw) {
            // room1 right of room0 (should be positive)
            return room1.lowLeft.getX() - (room0.lowLeft.getX() + room0.rw);
        }
        if (room0.lowLeft.getX() > room1.lowLeft.getX() + room1.rw) {
            // room0 right of room1 (should be negative)
            return -(room0.lowLeft.getX() - (room1.lowLeft.getX() + room1.rw));
        }
        return 0;
    }

    private int getDistanceY(Room room0, Room room1) {
        if (room1.lowLeft.getY() > room0.lowLeft.getY() + room0.rh) {
            // room1 above of room0 (should be positive)
            return room1.lowLeft.getY() - (room0.lowLeft.getY() + room0.rh);
        }
        if (room0.lowLeft.getY() > room1.lowLeft.getY() + room1.rh) {
            // room0 above of room1 (should be negative)
            return -(room0.lowLeft.getY() - (room1.lowLeft.getY() + room1.rh));
        }
        return 0;
    }

    private Point canDig(Point p, Point direction) {
        p = p.add(direction);
        if (isCandidate(p)) {
            return p;
        }
        return null;
    }

    private Point digCorridor(Point start, CorridorSegment segment, List<Point> corridorFields) {
        return digCorridor(start, segment.direction, segment.len, corridorFields);
    }

    private Point digCorridor(Point start, Point direction, int cnt, List<Point> corridorFields) {

        while (cnt-- > 0) {
            if ((start = canDig(start, direction)) == null) {
                return null;
            }
            corridorFields.add(start);
        }
        return start;
    }

    private List<Point> getCandidates() {
        List<Point> candidates = new ArrayList<>();
        loop(width, height, p -> {
            if (isCandidate(p)) {
                candidates.add(p);
            }
        });
        return candidates;
    }

    private void loop(int w, int h, PointVisitor visitor) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                visitor.visit(new Point(x, y));
            }
        }
    }

    /**
     * A candidate is an inside wall field surrounded completely by walls.
     *
     * @param p
     */
    private boolean isCandidate(Point p) {
        if (!p.insideXRange(1, width - 2)) {
            return false;
        }
        if (!p.insideYRange(1, height - 2)) {
            return false;
        }
        // p is inside
        IntHolder fieldNeighbors = new IntHolder(0);
        p.visitNeighbor(n -> {
            if (fields.contains(n)) {
                fieldNeighbors.inc();
            }
        });
        return fieldNeighbors.getValue() == 0;
    }

    private Point pickRandomPointFromList(List<Point> list) {
        if (list.size() == 0) {
            return null;
        }
        return list.get(intProvider.nextInt() % list.size());
    }

    private List<Point> getLowerUpperDoorCandidates(Room room, boolean lower) {
        List<Point> candidates = new ArrayList<>();
        //why not in the edges? for (int i = 1; i < room.rw - 1; i++) {
        for (int i = 0; i < room.rw; i++) {
            // TODO consider existing corridors
            candidates.add(new Point(room.lowLeft.getX() + i, (lower) ? room.lowLeft.getY() - 1 : room.lowLeft.getY() + room.rh));
        }
        return candidates;
    }

    private List<Point> getLeftRightDoorCandidates(Room room, boolean left) {
        List<Point> candidates = new ArrayList<>();
        //why not in the edges? for (int i = 1; i < room.rh - 1; i++) {
        for (int i = 0; i < room.rh; i++) {
            // TODO consider existing corridors
            candidates.add(new Point(left ? room.lowLeft.getX() - 1 : room.lowLeft.getX() + room.rw, room.lowLeft.getY() + i));
        }
        return candidates;
    }

    private Log getLogger() {
        return Platform.getInstance().getLog(MazeGeneratorRect.class);
    }
    /*private Room findClosestRoomByX(int min){
    for (Room room:rooms){
        if (room.lowLeft.getX())
    }
    }*/

    // Remebr C#
    class Room {
        public Point lowLeft;
        int rw, rh;

        public Room(Point lowLeft, int rw, int rh) {
            this.lowLeft = lowLeft;
            this.rw = rw;
            this.rh = rh;
        }
    }

    class Corridor {

        List<Point> corridorFields;

        public Corridor(List<Point> corridorFields) {
            this.corridorFields = corridorFields;
        }
    }

    class CorridorSegment {

        Point direction;
        int len;

        public CorridorSegment(Point direction, int len) {
            this.direction = direction;
            this.len = len;
        }
    }
}

class CorridorCandidate {

    int distance, turnOffset;
    public List<MazeGeneratorRect.CorridorSegment> corridorSegments = new ArrayList<MazeGeneratorRect.CorridorSegment>();
    public Point leave, enter;

    CorridorCandidate(/*int distance, int turnOffset*/) {
        this.distance = distance;
        this.turnOffset = turnOffset;
    }
}