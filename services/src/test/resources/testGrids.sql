-- for now with real newlines

insert into mazes.maze values(nextval('mazes.maze_seq'),'Sokoban Wikipedia',
'  ####n'||
'###  ####n'||
'#     $ #n'||
'# #  #$ #n'||
'# . .#@ #n'||
'#########',null,'The example from Wikipedia','S',now(),'admin',now(),'admin');

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
