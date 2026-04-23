package com.wig3003.multimedia.controller;

import javafx.fxml.FXML;

public class SocialSharingController {

    private SideNavBarController sideNavBarController;

    @FXML
    public void initialize() {
        System.out.println("SocialSharingController init");
        System.out.println("sideNavBarController is: " + sideNavBarController);
        
        if (sideNavBarController != null) {
            sideNavBarController.setActiveModuleButton("social");
        } else {
            System.out.println("ERROR: sideNavBarController is NULL");
        }
    }
}
