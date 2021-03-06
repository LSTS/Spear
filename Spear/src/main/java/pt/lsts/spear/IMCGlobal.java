package pt.lsts.spear;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;

import java.util.LinkedHashSet;
import java.util.List;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.VehicleState;
import pt.lsts.imc.net.ConnectFilter;
import pt.lsts.imc.net.IMCProtocol;
import pt.lsts.neptus.messages.listener.MessageInfo;

/**
 * Created by nachito on 26/03/17.
 */


@EBean(scope = Scope.Singleton)
public class IMCGlobal extends IMCProtocol {

    String selectedVehicle = null;
    private VehicleList veiculos;
    private PlanList planos;
    private PlanList maneuvers;

    IMCGlobal() {
        super();
        setAutoConnect(ConnectFilter.VEHICLES_ONLY);
        veiculos = new VehicleList();
        register(veiculos);

        planos = new PlanList(this);
        register(planos);
        maneuvers = new PlanList(this);
        register(maneuvers);

    }

    @Override
    public void onMessage(MessageInfo messageInfo, IMCMessage imcMessage) {
        super.onMessage(messageInfo, imcMessage);
    }

    String getSelectedvehicle() {
        return selectedVehicle;
    }

    void setSelectedvehicle(String selectedvehicle) {
        this.selectedVehicle = selectedvehicle;

    }

    List<VehicleState> connectedVehicles() {

        return veiculos.connectedVehicles();
    }

    LinkedHashSet<String> stillConnected() {
        return veiculos.stillConnected();
    }

    List<String> allPlans() {
        return planos.PlanLists(selectedVehicle);
    }

    List<String> allSensores() {
        return planos.SensorList(selectedVehicle);
    }

    List<Maneuver> allManeuvers() {
        return maneuvers.ManeuversLists(selectedVehicle);
    }

    void sendMessage(IMCMessage imcMessage) {
        sendMessage(getSelectedvehicle(), imcMessage);
    }

    void sendToAll(IMCMessage imcMessage) {
        for (VehicleState veh : connectedVehicles())
            sendMessage(veh.getSourceName(), imcMessage);
    }


}


