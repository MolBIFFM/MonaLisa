/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.exceptions;

import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;

public class PlaceNonConstantException extends Exception {

    public static final StringResources strings = ResourceManager.instance().getDefaultStrings();

    public PlaceNonConstantException() {
        super(strings.get("TSPlaceNonConstantException"));
    }
}
