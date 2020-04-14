/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.utils;

import javax.swing.JCheckBox;
import monalisa.data.pn.Place;

public class PlaceCheckBox extends JCheckBox {

    Place place;

    PlaceCheckBox(Place p) {
        super((String) p.getProperty("name"));
        this.place = p;
    }

    public Integer getID() {
        return place.id();
    }

    public String getPlaceName() {
        return place.getProperty("name");
    }
}
