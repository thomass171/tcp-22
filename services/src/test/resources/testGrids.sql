-- for now with real newlines

delete from mazes.maze;

-- secret is "Baskerville"
insert into mazes.maze values(nextval('mazes.maze_seq'),'Sokoban Wikipedia',
'  ####n'||
'###  ####n'||
'#     $ #n'||
'# #  #$ #n'||
'# . .#@ #n'||
'#########','$2a$10$VI1LXO9hqeDwjir7iJqxOO/1LiCrBhmdIVXtFKU2MVSZKl790r.PK','The example from Wikipedia','S',now(),'admin',now(),'admin');

insert into mazes.maze values(nextval('mazes.maze_seq'),'Sokoban 10x10',
'##########n'||
'#        #n'||
'#  $    .#n'||
'# #  $ . #n'||
'# ###   .#n'||
'#   # ####n'||
'#   #$#  #n'||
'#       @#n'||
'#        #n'||
'##########',null,'An easy 10x10 grid','S',now(),'admin',now(),'admin');
