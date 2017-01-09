package com.example.evge22pc.timetableapp.data;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.example.evge22pc.timetableapp.MainActivity;

import java.io.Serializable;


public class UniversityClass implements Serializable {
    private int id, int_id, week_day, week, num, class_type;
    private String name, homework, class_num;
    private Teacher teacher;
    private boolean pressed;

    public UniversityClass(){}

    public UniversityClass(int id, int int_id, int week_day,
                           int week, int num, int class_type,
                           String class_num, String name, String teacher,
                           String homework) {
        this.id = id;
        this.int_id = int_id;
        this.week_day = week_day;
        this.week = week;
        this.num = num;
        this.class_type = class_type;
        this.class_num = class_num;
        this.name = name;
        String[] tmp = teacher.split(" "); //TODO make this more beautiful
        if (tmp.length>1) {
            this.teacher = new Teacher(tmp[0],tmp[1],tmp[2]);
        } else this.teacher = new Teacher(tmp[0],null,null);
        this.homework = homework;
    }

    public int getId() {
        return id;
    }

    public int getInt_id() {
        return int_id;
    }

    public int getWeek_day() {
        return week_day;
    }

    public int getWeek() {
        return week;
    }

    public int getNum() {
        return num;
    }

    public int getClass_type() {
        return class_type;
    }

    public String getClass_num() {
        return class_num;
    }

    public String getName() {
        return name;
    }

    public String getTeacher() {
        return teacher.toString();
    }

    public String getHomework() {
        return homework.equals("null") ? "" : homework;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInt_id(int int_id) {
        this.int_id = int_id;
    }

    public void setWeek_day(int week_day) {
        this.week_day = week_day;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setClass_type(int class_type) {
        this.class_type = class_type;
    }

    public void setClass_num(String class_num) {
        this.class_num = class_num;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public void setHomework(String homework) {
        this.homework = homework;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>").append(num).append(".</b> ").append(name).append(
                (class_type==1) ? " лекц. ауд. " : " практ. ауд. ").append(class_num).append(" ").append(
                teacher.toString());
        if (pressed) {
            if (homework != "null" && class_type == 0)
                stringBuilder.append(" Д/з: ").append(homework); //TODO make one more clause come from SharedPrefs
        }
        return stringBuilder.toString();
    }

    private class Teacher implements Serializable{
        private String name, surname, fathers;

        public Teacher(String surname, @Nullable String name, @Nullable String fathers) {
            this.surname = surname;
            if (name!=null) this.name = name;
            else this.name = "";
            if (fathers!=null) this.fathers = fathers;
            else this.fathers = "";
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getFathers() {
            return fathers;
        }

        public void setFathers(String fathers) {
            this.fathers = fathers;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            if (pressed) {
                stringBuilder.append(surname).append(" ").append(name).append(" ").append(fathers);
            } else {
                stringBuilder.append(surname).append(" ").append((name.length() >= 1 ? name.charAt(0) + "." : "")).append((fathers.length() >= 1 ? fathers.charAt(0) + "." : ""));
            }
            return stringBuilder.toString();
        }
    }
}
