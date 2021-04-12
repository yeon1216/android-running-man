package com.example.teamproject.object;

public class Game {
    public int game_no;
    public String game_name;
    public String game_admin_id;
    public String game_join_user;
    public int is_game_exit;

    public Game() {
    }

    public Game(int game_no, String game_name, String game_admin_id, String game_join_user, int is_game_exit) {
        this.game_no = game_no;
        this.game_name = game_name;
        this.game_admin_id = game_admin_id;
        this.game_join_user = game_join_user;
        this.is_game_exit = is_game_exit;
    }

    public void setGame_no(int game_no) {
        this.game_no = game_no;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public void setGame_admin_id(String game_admin_id) {
        this.game_admin_id = game_admin_id;
    }

    public void setGame_join_user(String game_join_user) {
        this.game_join_user = game_join_user;
    }

    public void setIs_game_exit(int is_game_exit) {
        this.is_game_exit = is_game_exit;
    }

    public int getGame_no() {
        return game_no;
    }

    public String getGame_name() {
        return game_name;
    }

    public String getGame_admin_id() {
        return game_admin_id;
    }

    public String getGame_join_user() {
        return game_join_user;
    }

    public int getIs_game_exit() {
        return is_game_exit;
    }
}
