package fr.univ_amu.ihm;

public interface PanelObserver {

    public void updateExternalControlPanel(ExternalControlPanel externalControlPanel);
    public void updateInternalControlPanel(InternalControlPanel internalControlPanel);
    public void notifyEmergencyStop();

}
