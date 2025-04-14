package fctreddit.impl.server.java;

import java.util.List;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.java.Users;
import fctreddit.impl.server.persistence.Hibernate;

public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private Hibernate hibernate;

    public JavaUsers() {
        hibernate = Hibernate.getInstance();
    }

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if (user == null || user.getUserId() == null || user.getPassword() == null || user.getPassword().equals("") || user.getFullName() == null
                || user.getEmail() == null) {
            Log.info("User object invalid.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        try {
            hibernate.persist(user);
        } catch (Exception e) {
            e.printStackTrace(); //Most likely the exception is due to the user already existing...
            Log.info("User already exists.");
            return Result.error(ErrorCode.CONFLICT);
        }

        return Result.ok(user.getUserId());
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);
        // Check if user is valid
        if (userId == null) {
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        User user = null;
        try {
            user = hibernate.get(User.class, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }

        // Check if user exists
        if (user == null) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Check if the password is correct
        if (!user.getPassword().equals(password)) {
            Log.info("Password is incorrect");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        return Result.ok(user);

    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; userData = " + user);
        // Check if user is valid
        if (userId == null || password == null ) {
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        User updatedUser = null;
        try {
            updatedUser = hibernate.get(User.class, userId);
            if (updatedUser == null) {
                Log.info("User does not exist.");
                return Result.error(ErrorCode.NOT_FOUND);
            }
            if (!updatedUser.getPassword().equals(password)) {
                Log.info("Password is incorrect");
                return Result.error(ErrorCode.FORBIDDEN);
            }
            if (user.getPassword() != null) {
                updatedUser.setPassword(user.getPassword());
            }
            if (user.getFullName() != null) {
                updatedUser.setFullName(user.getFullName());
            }
            if (user.getEmail() != null) {
                updatedUser.setEmail(user.getEmail());
            }
            if (user.getPassword() != null) {
                updatedUser.setPassword(user.getPassword());
            }
            hibernate.update(updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
        // TODO Auto-generated method stub
        return Result.ok(updatedUser);
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        if (userId == null || password == null) {
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            User user = hibernate.get(User.class, userId);
            if (user == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }

            if (!user.getPassword().equals(password)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }

            hibernate.delete(user);
            return Result.ok(user); // return the deleted user
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }


    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);
        List<User> list = null;
        try {
            list = hibernate.jpql("SELECT u FROM User u WHERE u.userId LIKE '%" + pattern +"%'", User.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
        return Result.ok(list);

    }


}

