package de.dbvis.htpm;

import de.dbvis.htpm.util.HTPMListener;

public interface TemporalPatternProducer {

    /**
     * tells the producer to start producing hybrid temporal patterns
     */
    void start();

    /**
     * Adds an HTPMListener, which receives update events about the pattern mining process.
     * @param l - the HTPMListener to be added.
     */
    void addHTPMListener(HTPMListener l);

    /**
     * Removes an HTPMListener.
     * @param l - the HTPMListener to be removed.
     */
    void removeHTPMListener(HTPMListener l);
}
