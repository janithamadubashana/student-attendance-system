create table if not exists Student(
    id varchar(20) primary key  ,
    name varchar(100) not null
);
create table if not exists Picture(
  picture MEDIUMBLOB not null ,
  student_id varchar(20) primary key ,
  constraint fk_student_picture foreign key (student_id) references Student(id)
);

create table if not exists Attendance(
  id int primary key auto_increment,
  status enum('IN','OUT') not null ,
  stamp datetime not null ,
  student_id varchar(20) not null ,
  constraint fk_student_attendance foreign key (student_id) references Student(id)
);

create table if not exists User(
  username varchar(50) primary key ,
  password varchar(100) not null ,
  full_name varchar(100) not null
);