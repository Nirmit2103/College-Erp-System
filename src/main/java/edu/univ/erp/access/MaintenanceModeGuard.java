package edu.univ.erp.access;

import edu.univ.erp.domain.MaintenanceSetting;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicReference;

public class MaintenanceModeGuard {

    private final AtomicReference<MaintenanceSetting> current = new AtomicReference<>();
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public void update(MaintenanceSetting setting) {
        MaintenanceSetting previous = current.getAndSet(setting);
        changeSupport.firePropertyChange("maintenance", previous, setting);
    }

    public boolean isMaintenanceOn() {
        MaintenanceSetting setting = current.get();
        return setting != null && setting.maintenanceOn();
    }                                                                   

    public MaintenanceSetting currentSetting() {
        return current.get();
    }

    public void addListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
}

