package fctreddit.clients.rest;

import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.impl.server.java.JavaContent;
import fctreddit.impl.server.java.JavaImage;
import fctreddit.impl.server.java.JavaUsers;
//import fctreddit.impl.server.persistence.Hibernate;

import java.io.IOException;
import java.util.Scanner;

//import fctreddit.impl.server.persistence.Hibernate;



public class test {


    public static void main(String[] args) throws IOException {

        Scanner in = new Scanner(System.in);
        //Hibernate hibernate;

        //String line = in.nextLine();
        //String[] argss = line.split(" ");

        String userId = "a";
        String fullName = "b";
        String email = "c";
        String password = "d";


        User usr = new User(userId, fullName, email, password);
        JavaUsers js = new JavaUsers();
        JavaImage ji = new JavaImage();

        js.createUser(usr);
        js.getUser(userId, password);
        byte[] image = new byte[10];
        image[0] = 1;
        image[1] = 1;

        ji.createImage(userId, image, password);
    }
}
