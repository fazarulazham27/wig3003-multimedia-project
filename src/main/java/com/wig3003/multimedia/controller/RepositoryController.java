package com.wig3003.multimedia.controller;

import javafx.fxml.FXML;

public class RepositoryController implements SideNavBarController.IFilterableModule {

    @FXML
    public void initialize() {
        System.out.println("Repository module loaded");
    }

    @Override
    public void applyFilter(String filter) {
        System.out.println("Repository filter applied: " + filter);
    }
}
