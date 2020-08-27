package com.lrh.netty.binary.serializerobject;

import java.io.Serializable;

/**
 * 实体类
 *
 * @Author lrh 2020/8/26 14:28
 */
public class Student implements Serializable {
    private String name;
    private int age;
    private String gender;

    public Student(String name, int age, int gender) {
        this.name = name;
        this.age = age;
        this.gender = gender==1?"男":"女";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                '}';
    }
}
