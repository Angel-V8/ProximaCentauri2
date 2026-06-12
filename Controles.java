import controlP5.*;
import processing.core.PApplet;
import processing.data.XML;
import java.io.File;

public class Controles {
  private RescatProxima app;
  private ControlP5 cp5;

  public Controles(RescatProxima app) {
    this.app = app;
    this.cp5 = new ControlP5(app);
    setupControls();
  }

  public ControlP5 getCP5() {
    return this.cp5;
  }

  private void setupControls() {
    // --- CONTROLS DEL MENÚ PRINCIPAL ---
    cp5.addButton("btnNovaPartida")
      .setPosition(app.width/2 - 100, app.height/2 - 50)
      .setSize(200, 45)
      .plugTo(app, "iniciarJoc");

    cp5.addButton("btnCarregarPartida")
      .setPosition(app.width/2 - 100, app.height/2 + 5)
      .setSize(200, 45)
      .plugTo(app, "carregarPartida");

    cp5.addButton("btnConfig")
      .setPosition(app.width/2 - 100, app.height/2 + 60)
      .setSize(200, 45)
      .plugTo(app, "obrirConfiguracio");

    // Selector de llenguatge en format ScrollableList (desplegable)
    ScrollableList listIdioma = cp5.addScrollableList("desplegableIdioma")
      .setPosition(app.width/2 - 100, app.height/2 + 115)
      .setSize(200, 180)
      .setBarHeight(45)
      .setItemHeight(45)
      .addItem("VALENCIÀ", 0)
      .addItem("ENGLISH", 1)
      .addItem("CASTELLANO", 2)
      .close();
      
    listIdioma.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
    listIdioma.plugTo(app, "desplegableIdioma");

    // --- CONTROLS DEL MENÚ DE PAUSA ---
    cp5.addButton("btnSeguir")
      .setPosition(app.width/2 - 120, app.height/2 - 25)
      .setSize(240, 45)
      .setColorBackground(app.color(30, 150, 80))
      .setColorForeground(app.color(40, 180, 100))
      .setColorActive(app.color(50, 200, 120))
      .plugTo(app, "resumirJoc");

    cp5.addButton("btnGuardarSortir")
      .setPosition(app.width/2 - 120, app.height/2 + 35)
      .setSize(240, 45)
      .setColorBackground(app.color(180, 50, 50))
      .setColorForeground(app.color(210, 60, 60))
      .setColorActive(app.color(240, 70, 70))
      .plugTo(app, "guardarIEixir");

    // --- CONTROLS DE CONFIGURACIÓ IN-GAME ---
    cp5.addToggle("toggleDificultat")
      .setLabel("")
      .setPosition(app.width/2 - 50, app.height/2 - 90)
      .setSize(60, 25)
      .setMode(ControlP5.SWITCH)
      .setState(app.configJSON.getInt("dificultat") == 1)
      .setColorActive(app.color(255, 50, 80))
      .setColorForeground(app.color(180, 30, 50))
      .setColorBackground(app.color(40, 40, 60))
      .plugTo(app, "canviarDificultat");

    // Parallax toggle usa verd neó per defecte
    cp5.addToggle("toggleParallax")
      .setLabel("")
      .setPosition(app.width/2 - 50, app.height/2 - 30)
      .setSize(60, 25)
      .setMode(ControlP5.SWITCH)
      .setState(app.configJSON.getBoolean("desactivarParallax"))
      .setColorActive(app.color(0, 255, 128))
      .setColorForeground(app.color(0, 200, 100))
      .setColorBackground(app.color(40, 40, 60))
      .plugTo(app, "canviarParallax");

    // FPS toggle usa verd neó per defecte
    cp5.addToggle("toggleFPS")
      .setLabel("")
      .setPosition(app.width/2 - 50, app.height/2 + 30)
      .setSize(60, 25)
      .setMode(ControlP5.SWITCH)
      .setState(app.configJSON.getBoolean("mostrarFPS"))
      .setColorActive(app.color(0, 255, 128))
      .setColorForeground(app.color(0, 200, 100))
      .setColorBackground(app.color(40, 40, 60))
      .plugTo(app, "canviarFPS");

    cp5.addButton("btnBoss")
      .setPosition(app.width/2 - 220, app.height/2 + 110)
      .setSize(210, 45)
      .setColorBackground(app.color(200, 30, 80))
      .setColorForeground(app.color(255, 50, 120))
      .setColorActive(app.color(255, 100, 150))
      .plugTo(app, "iniciarBoss");

    cp5.addButton("btnTornar")
      .setPosition(app.width/2 + 10, app.height/2 + 110)
      .setSize(210, 45)
      .setColorBackground(app.color(30, 80, 200))
      .setColorForeground(app.color(50, 120, 255))
      .setColorActive(app.color(100, 150, 255))
      .plugTo(app, "tornarAlMenu");
  }

  public void actualitzarVisibilitatMenus() {
    if (cp5.get("btnNovaPartida") != null) cp5.get("btnNovaPartida").hide();
    if (cp5.get("btnCarregarPartida") != null) cp5.get("btnCarregarPartida").hide();
    if (cp5.get("btnConfig") != null) cp5.get("btnConfig").hide();
    if (cp5.get("desplegableIdioma") != null) cp5.get("desplegableIdioma").hide();
    if (cp5.get("toggleDificultat") != null) cp5.get("toggleDificultat").hide();
    if (cp5.get("toggleParallax") != null) cp5.get("toggleParallax").hide();
    if (cp5.get("toggleFPS") != null) cp5.get("toggleFPS").hide();
    if (cp5.get("btnBoss") != null) cp5.get("btnBoss").hide();
    if (cp5.get("btnTornar") != null) cp5.get("btnTornar").hide();
    if (cp5.get("btnSeguir") != null) cp5.get("btnSeguir").hide();
    if (cp5.get("btnGuardarSortir") != null) cp5.get("btnGuardarSortir").hide();

    if (app.estatJoc == -1) {
      if (app.enConfiguracio) {
        cp5.get("toggleDificultat").show();
        cp5.get("toggleParallax").show();
        cp5.get("toggleFPS").show();
        cp5.get("btnBoss").show();
        cp5.get("btnTornar").show();
      } else {
        cp5.get("btnNovaPartida").show();
        cp5.get("btnConfig").show();
        
        ScrollableList list = (ScrollableList) cp5.get("desplegableIdioma");
        if (list != null) {
          list.show();
        }

        Button btnCarregar = cp5.get(Button.class, "btnCarregarPartida");
        if (btnCarregar != null) {
          btnCarregar.show();
          File f = new File(app.dataPath("partida.json"));
          if (f.exists()) {
            btnCarregar.setLock(false)
                       .setColorBackground(app.color(0, 45, 90))
                       .setColorForeground(app.color(0, 116, 217))
                       .setColorActive(app.color(0, 160, 240));
          } else {
            btnCarregar.setLock(true)
                       .setColorBackground(app.color(80, 80, 80))
                       .setColorForeground(app.color(80, 80, 80))
                       .setColorActive(app.color(80, 80, 80));
          }
        }
      }
    } else if (app.estatJoc == 0) {
      if (app.enPausa) {
        cp5.show();
        cp5.get("btnSeguir").show();
        cp5.get("btnGuardarSortir").show();
      } else {
        cp5.hide();
      }
    } else {
      cp5.hide();
    }
  }

  public void aplicarIdioma() {
    try {
      XML xmlSencer = app.loadXML("data/idiomes.xml");
      XML xmlIdioma = xmlSencer.getChild(app.idiomaActual);

      app.textTitol = xmlIdioma.getChild("titol").getContent();
      String textConfig = xmlIdioma.getChild("config").getContent();
      String textBoss = xmlIdioma.getChild("boss") != null ? xmlIdioma.getChild("boss").getContent() : "JUGAR BOSS";
      String textTornar = xmlIdioma.getChild("tornar") != null ? xmlIdioma.getChild("tornar").getContent() : "TORNAR";
      String textNova = xmlIdioma.getChild("nova") != null ? xmlIdioma.getChild("nova").getContent() : "NOVA PARTIDA";
      String textCarregar = xmlIdioma.getChild("carregar") != null ? xmlIdioma.getChild("carregar").getContent() : "CARREGAR PARTIDA";
      String textSeguir = xmlIdioma.getChild("seguir") != null ? xmlIdioma.getChild("seguir").getContent() : "TORNAR AL JOC";
      String textGuardar = xmlIdioma.getChild("guardar") != null ? xmlIdioma.getChild("guardar").getContent() : "GUARDAR I EIXIR";

      cp5.get(controlP5.Button.class, "btnConfig").setLabel(textConfig);
      if (cp5.get(controlP5.Button.class, "btnNovaPartida") != null) {
        cp5.get(controlP5.Button.class, "btnNovaPartida").setLabel(textNova);
      }
      if (cp5.get(controlP5.Button.class, "btnCarregarPartida") != null) {
        cp5.get(controlP5.Button.class, "btnCarregarPartida").setLabel(textCarregar);
      }
      if (cp5.get(controlP5.Button.class, "btnSeguir") != null) {
        cp5.get(controlP5.Button.class, "btnSeguir").setLabel(textSeguir);
      }
      if (cp5.get(controlP5.Button.class, "btnGuardarSortir") != null) {
        cp5.get(controlP5.Button.class, "btnGuardarSortir").setLabel(textGuardar);
      }
      if (cp5.get(controlP5.Button.class, "btnBoss") != null) {
        cp5.get(controlP5.Button.class, "btnBoss").setLabel(textBoss);
      }
      if (cp5.get(controlP5.Button.class, "btnTornar") != null) {
        cp5.get(controlP5.Button.class, "btnTornar").setLabel(textTornar);
      }
      ScrollableList list = (ScrollableList) cp5.get("desplegableIdioma");
      if (list != null) {
        if (app.idiomaActual.equals("cat")) {
          list.getCaptionLabel().set("VALENCIÀ");
        } else if (app.idiomaActual.equals("eng")) {
          list.getCaptionLabel().set("ENGLISH");
        } else if (app.idiomaActual.equals("esp")) {
          list.getCaptionLabel().set("CASTELLANO");
        }
      }
    }
    catch (Exception e) {
      System.out.println("Error carregant idiomes.xml");
    }
  }

  public void show() {
    cp5.show();
  }

  public void hide() {
    cp5.hide();
  }
}
