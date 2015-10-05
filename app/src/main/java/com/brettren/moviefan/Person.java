package com.brettren.moviefan;


public class Person {
    public String biography;
    public String birthday;
    public String name;
    public String place_of_birth;
    public String profile_path;
    public String id;
    public String character;

    public Person(){
    }

    public Person(String id, String character, String name, String profile_path){
        this.id = id;
        this.character = character;
        this.name = name;
        this.profile_path = profile_path;
    }

    public Person(String id, String name, String profile_path, String biography, String birthday, String place_of_birth){
        this.id = id;
        this.name = name;
        this.profile_path = profile_path;
        this.biography = biography;
        this.birthday = birthday;
        this.place_of_birth = place_of_birth;
    }
}
