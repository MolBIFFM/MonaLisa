/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Molekulare Bioinformatik, Goethe University Frankfurt, Frankfurt am Main, Germany
 *
 */

package monalisa.addons;

import java.util.Map;

/**
 * The Interface needed by a AddOn to store data in the project file and receive those data.
 * @author jens
 */
public interface AddonStorageManagment {

    /**
     * Provides a map of the objects with their identifier, which should be stored in the project file.
     * @return Map<String, Object>
     */
    public Map<String, Object> getObjectsForStorage();

    /**
     * This function is called, after the project is loaded and gives the Addon class their stored data back.
     * @param storage
     */
    public void reciveStoredObjects(Map<String, Object> storage);

}
