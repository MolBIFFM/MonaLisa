/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.annotations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jens Einloft
 */
public class DateWrapper {

    private final Date date;
    private final int year;
    private final int month;
    private final int day;
    private final Logger LOGGER = LogManager.getLogger(DateWrapper.class);

    public DateWrapper(String year, String month, String day) {
        LOGGER.debug("Generating new DateWrapper");
        this.year = new Integer(year);
        this.day = Integer.parseInt(day);
        this.month = new Integer(month);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        long timestamp = 0;
        try {
            LOGGER.debug("Generating timestamp");
            timestamp = dateFormat.parse(day+"/"+month+"/"+year+" 12:00:00").getTime();
        } catch (ParseException ex) {
            LOGGER.error(ex);
        }

        this.date = new Date(timestamp);
        LOGGER.debug("Successfully generated new DateWrapper");
    }

    public Date getDate() {
        return this.date;
    }

    public String getYear() {
        return Integer.toString(this.year);
    }

    public String getMonth() {
        return Integer.toString(this.month);
    }

    public String getDay() {
        return Integer.toString(this.day);
    }

    @Override
    public String toString() {
        return this.year+"-"+this.month+"-"+this.day;
    }

}
