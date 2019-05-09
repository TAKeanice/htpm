module de.dbvis.htpm {
    requires java.xml;
    requires java.desktop;
    requires org.apache.commons.lang3;

    exports de.dbvis.htpm;
    exports de.dbvis.htpm.occurrence;
    exports de.dbvis.htpm.constraints;
    exports de.dbvis.htpm.db;
    exports de.dbvis.htpm.hes;
    exports de.dbvis.htpm.hes.events;
    exports de.dbvis.htpm.htp;
    exports de.dbvis.htpm.util;
    exports de.dbvis.htpm.htp.eventnodes;
}
