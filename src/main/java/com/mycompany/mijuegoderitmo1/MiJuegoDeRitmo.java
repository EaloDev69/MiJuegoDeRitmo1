/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mijuegoderitmo1;


import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.audio.AudioNode;
import States.MenuAppState;
import java.util.List;
/**
 *
 * @author CamiLaNekoUwU_Gamer
 */
public class MiJuegoDeRitmo extends SimpleApplication {

    private MenuAppState menuAppStates;
    private AppState gameplayAppState; 
    private float masterVolume = 1.0f;
    private AudioNode currentSong;
    
    public static void main(String[] args) {
        MiJuegoDeRitmo app = new MiJuegoDeRitmo();
        app.start(); 
    }

    @Override
    public void simpleInitApp() {

        this.setDisplayFps(true);
        this.setDisplayStatView(false);

        menuAppStates = new MenuAppState();
        stateManager.attach(menuAppStates);
    }

    /**
     *
     * @param cancionesSeleccionadas 
     */
    public void startGameplay(List<String> cancionesSeleccionadas) {
        System.out.println("Iniciando gameplay con canciones: " + cancionesSeleccionadas);

        // Limpia el menú
        if (menuAppStates != null) {
            stateManager.detach(menuAppStates);
        }

        
    }
    public void setMasterVolume(float volume) {
        this.masterVolume = volume;
        if (currentSong != null) {
            currentSong.setVolume(volume);
        }
    }

    public float getMasterVolume() {
        return masterVolume;
    }


    public void volverAlMenu() {
        if (gameplayAppState != null) {
            stateManager.detach(gameplayAppState);
            gameplayAppState = null;
        }

        if (menuAppStates == null) {
            menuAppStates = new MenuAppState();
        }
        stateManager.attach(menuAppStates);
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {

    }


    public Node getGuiNode() {
        return guiNode;
    }

    
}


