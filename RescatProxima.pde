import java.util.ArrayList;
import controlP5.*;

JSONObject configJSON;
Controles controles;

// Variables Globals
PImage fonsInici;
Pantalla nivellActual;
int contadorFramesNivell = 0;
int numeroNivell = 1; // Comença al nivell 1
int estatJoc = -1;
boolean enConfiguracio = false; // Indica si estem al menú de configuració
boolean enPausa = false; // Indica si el joc està en pausa
int tempsTransicio = 0;

String idiomaActual = "cat";
String textTitol = "";

Marcador marcador;
NauPlayer jugador;
MonstreFinal boss; // NOU: Referència global al boss final

// Llistes Globals
ArrayList<Enemic> llistaEnemics;
ArrayList<Meteorit> llistaMeteorits;
ArrayList<Booster> llistaBoosters;

// Les bales enemigues i mines ara viuen al Main!
ArrayList<Dispar> balesEnemigues;
ArrayList<Mina> llistaMines;

int intervalSpawn = 2000;
int framesIntervalSpawn = 120; // NOU: Pre-calculat per a optimització
int framesIntervalMeteorits = 0; // NOU: Pre-calculat per a optimització

void setup() {
  size(800, 600);

  fonsInici = loadImage("./img/inici.png");

  jugador = new NauPlayer(this);
  marcador = new Marcador(this);

  llistaEnemics = new ArrayList<Enemic>();
  llistaMeteorits = new ArrayList<Meteorit>();
  llistaBoosters = new ArrayList<Booster>();
  balesEnemigues = new ArrayList<Dispar>();
  llistaMines = new ArrayList<Mina>();

  carregarNivell(numeroNivell);

  carregarConfiguracio();

  // CONFIGURACIÓ DEL MENÚ I CONTROLS CP5
  controles = new Controles(this);

  // Ajustem la visibilitat inicial dels dos menús
  enConfiguracio = false;
  actualitzarVisibilitatMenus();

  aplicarIdioma();
}

void draw() {
  if (estatJoc == -1) {
    imageMode(CORNER);
    if (fonsInici != null) {
      image(fonsInici, 0, 0, width, height);
    } else {
      background(0);
    }

    if (enConfiguracio) {
      // Panel de fons translúcid per al menú de configuració
      fill(10, 15, 30, 220); // Blavós fosc transparent molt premium
      rectMode(CENTER);
      rect(width/2, height/2, 540, 460, 20); // Caixa del menú de configuració
      
      // Títol
      fill(0, 255, 128); // Verd neó
      textAlign(CENTER, CENTER);
      textSize(36);
      text(idiomaActual.equals("cat") ? "CONFIGURACIÓ" : "SETTINGS", width/2, height/2 - 180);
      
      // Etiquetes dels controls
      fill(255);
      textSize(15);
      textAlign(RIGHT, CENTER);
      float labelX = width/2 - 70; // 330 (20px a l'esquerra del control)
      text(idiomaActual.equals("cat") ? "DIFICULTAT:" : "DIFFICULTY:", labelX, height/2 - 77.5f);
      text(idiomaActual.equals("cat") ? "DESACTIVAR PARALLAX:" : "DISABLE PARALLAX:", labelX, height/2 - 17.5f);
      text(idiomaActual.equals("cat") ? "MOSTRAR FPS:" : "SHOW FPS:", labelX, height/2 + 42.5f);

      // Estat dels toggles escrit a mà
      textAlign(LEFT, CENTER);
      textSize(14);
      
      // Dificultat
      boolean difDificil = controles.getCP5().get(Toggle.class, "toggleDificultat").getState();
      fill(difDificil ? color(255, 50, 80) : color(0, 220, 255));
      text(difDificil ? (idiomaActual.equals("cat") ? "DIFÍCIL" : "HARD") : (idiomaActual.equals("cat") ? "NORMAL" : "NORMAL"), width/2 + 30, height/2 - 77.5f);
      
      // Parallax
      boolean parallaxDesactivat = controles.getCP5().get(Toggle.class, "toggleParallax").getState();
      fill(parallaxDesactivat ? color(255, 50, 80) : color(0, 255, 128));
      text(parallaxDesactivat ? (idiomaActual.equals("cat") ? "SÍ" : "YES") : "NO", width/2 + 30, height/2 - 17.5f);
      
      // FPS
      boolean fpsActiu = controles.getCP5().get(Toggle.class, "toggleFPS").getState();
      fill(fpsActiu ? color(0, 255, 128) : color(150));
      text(fpsActiu ? (idiomaActual.equals("cat") ? "SÍ" : "YES") : "NO", width/2 + 30, height/2 + 42.5f);
    } else {
      // Títol del joc normal
      fill(0, 255, 0);
      textAlign(CENTER, CENTER);
      textSize(60);
      text(textTitol, width/2, height/3 - 50);
    }
  } else if (estatJoc == 0) {
    if (enPausa) {
      imageMode(CORNER);
      rectMode(CORNER);
      textAlign(LEFT);
      nivellActual.dibuixarFons(configJSON.getBoolean("desactivarParallax"));

      for (Enemic e : llistaEnemics) {
        e.mostrar(this);
      }
      for (Meteorit met : llistaMeteorits) {
        met.mostrar(this);
      }
      for (Booster b : llistaBoosters) {
        b.mostrar(this);
      }
      for (Dispar d : balesEnemigues) {
        d.mostrar(this);
      }
      for (Mina m : llistaMines) {
        m.mostrar(this);
      }
      if (boss != null && boss.isActiu()) {
        boss.mostrar(this);
      }

      marcador.mostra(jugador.getVida());

      if (configJSON.getBoolean("mostrarFPS")) {
        pushStyle();
        fill(255, 255, 0); // Groc
        textSize(16);
        textAlign(RIGHT);
        text("FPS: " + Math.round(frameRate), width - 20, 75);
        popStyle();
      }

      jugador.mostrar(this);

      pushStyle();
      fill(10, 15, 30, 200); // Fons blau fosc transparent
      rectMode(CENTER);
      rect(width/2, height/2, 360, 260, 20); // Caixa del menú de pausa
      
      fill(0, 255, 128); // Verd neó
      textAlign(CENTER, CENTER);
      textSize(30);
      text(idiomaActual.equals("cat") ? "JOC EN PAUSA" : "GAME PAUSED", width/2, height/2 - 80);
      popStyle();
      return;
    }

    contadorFramesNivell++;
    generarMeteorits();

    boolean superado = false;
    if (nivellActual.getDurada() > 0) {
      int framesNecessaris = nivellActual.getDurada() * 60;
      if (contadorFramesNivell >= framesNecessaris) superado = true;
    } else {
      if (nivellActual.nivellSuperat(marcador.getScore())) superado = true;
    }

    if (superado) {
      estatJoc = 1;
      tempsTransicio = millis();
    }

    imageMode(CORNER);
    rectMode(CORNER);
    textAlign(LEFT);
    nivellActual.dibuixarFons(configJSON.getBoolean("desactivarParallax"));

    generarEnemics();
    generarBoosters();
    marcador.actualitza(contadorFramesNivell);
    marcador.mostra(jugador.getVida());

    if (configJSON.getBoolean("mostrarFPS")) {
      pushStyle();
      fill(255, 255, 0); // Groc
      textSize(16);
      textAlign(RIGHT);
      text("FPS: " + Math.round(frameRate), width - 20, 75);
      popStyle();
    }

    jugador.actualitzar();
    jugador.mostrar(this);

    // ==========================================
    // 1. GESTIÓ PROJECTILS ENEMICS I MINES
    // ==========================================
    for (int i = balesEnemigues.size() - 1; i >= 0; i--) {
      Dispar d = balesEnemigues.get(i);
      d.actualitzar();
      d.mostrar(this);

      if (d.getPosicio().x < -50) {
        balesEnemigues.remove(i);
      } else if (jugador.colisionaAmb(d)) {
        jugador.rebreDany(10);
        balesEnemigues.remove(i); // Desapareix al donar-te
      }
    }

    for (int i = llistaMines.size() - 1; i >= 0; i--) {
      Mina m = llistaMines.get(i);
      m.actualitzar();
      m.mostrar(this);

      if (m.getPosicio().x < -50 || m.haAcabatExplosio()) {
        llistaMines.remove(i);
        continue;
      }

      if (m.isExplotant()) {
        // Dany de l'explosió de la mina al jugador (només 1 vegada per mina)
        if (!m.haDanyatJugador() && Utils.hiHaColisio(jugador.getPosicio(), jugador.getTamany(), m.getPosicio(), m.getRadiExplosio() * 2)) {
          jugador.rebreDany(20);
          m.setHaDanyatJugador(true);
        }

        // L'explosió danya enemics propers (només una vegada per enemic)
        for (int j = llistaEnemics.size() - 1; j >= 0; j--) {
          Enemic e = llistaEnemics.get(j);
          if (!e.isDestruint() && !m.haDanyat(e) && Utils.hiHaColisio(e.getPosicio(), e.getTamany(), m.getPosicio(), m.getRadiExplosio() * 2)) {
            e.rebreDany(30); // Dany pesat per l'ona expansiva
            m.afegirDanyat(e);
            if (e.estaDestruit()) {
              incrementarPunts(100);
            }
          }
        }

        // L'explosió danya meteorits propers (només una vegada per meteorit)
        for (int j = llistaMeteorits.size() - 1; j >= 0; j--) {
          Meteorit met = llistaMeteorits.get(j);
          if (!met.isDestruint() && !m.haDanyat(met) && Utils.hiHaColisio(met.getPosicio(), met.getTamany(), m.getPosicio(), m.getRadiExplosio() * 2)) {
            met.rebreDany(30);
            m.afegirDanyat(met);
            if (met.estaDestruit()) {
              incrementarPunts(20);
            }
          }
        }
      } else {
        // Si no està explotant encara, comprovem col·lisió directa amb el jugador
        if (jugador.colisionaAmb(m)) {
          jugador.rebreDany(20);
          m.detonar(); // Comença l'animació d'explosió
          m.setHaDanyatJugador(true); // Evita rebre més dany del mateix blast
        }
      }
    }

    // ==========================================
    // 2. GESTIÓ METEORITS I BOOSTERS
    // ==========================================
    for (int i = llistaMeteorits.size() - 1; i >= 0; i--) {
      Meteorit m = llistaMeteorits.get(i);
      m.actualitzar();
      m.mostrar(this);

      if (m.getPosicio().x < -50 || m.haAcabatExplosio()) {
        llistaMeteorits.remove(i);
        continue;
      }
      if (!m.isDestruint() && jugador.colisionaAmb(m)) {
        jugador.rebreDany(20);
        m.rebreDany(100); // Es destrueix (entra en estat de destrucció)
      }
    }

    for (int i = llistaBoosters.size() - 1; i >= 0; i--) {
      Booster b = llistaBoosters.get(i);
      b.actualitzar();
      b.mostrar(this);

      if (b.getPosicio().x < -50) {
        llistaBoosters.remove(i);
        continue;
      }
      if (jugador.colisionaAmb(b)) {
        if (b instanceof BoosterVida) {
          jugador.curar(25);
        } else if (b instanceof BoosterAtac) {
          jugador.activarDobleDispar(600); // Actiu durant 10 segons (60 * 10)
        } else if (b instanceof BoosterEscut) {
          jugador.afegirEscut();
        }

        incrementarPunts(50);
        llistaBoosters.remove(i);
      }
    }

    // ==========================================
    // 3. GESTIÓ D'ENEMICS
    // ==========================================
    for (int i = llistaEnemics.size() - 1; i >= 0; i--) {
      Enemic e = llistaEnemics.get(i);
      e.actualitzar();
      e.mostrar(this);

      if (e.getPosicio().x < -50 || e.haAcabatExplosio()) {
        llistaEnemics.remove(i);
        continue;
      }

      if (!e.isDestruint()) {
        if (e instanceof Interceptor) {
          Dispar nouDispar = ((Interceptor) e).disparar();
          if (nouDispar != null) balesEnemigues.add(nouDispar);
        } else if (e instanceof Miner) {
          Mina novaMina = ((Miner) e).deixarMina();
          if (novaMina != null) llistaMines.add(novaMina);
        }

        if (jugador.colisionaAmb(e)) {
          jugador.rebreDany(15);
          e.rebreDany(100);
          if (e.estaDestruit()) {
            incrementarPunts(50);
          }
        }
      }
    }

    // ==========================================
    // 3.5 GESTIÓ DEL JEFE FINAL (MONSTRE FINAL)
    // ==========================================
    if (boss != null && boss.isActiu()) {
      boss.actualitzar();
      boss.mostrar(this);

      // El boss llança atacs a les bales enemigues generals del main
      ArrayList<Dispar> disparsBoss = boss.disparar(jugador.getPosicio());
      if (disparsBoss.size() > 0) {
        balesEnemigues.addAll(disparsBoss);
      }

      // Col·lisió directa jugador contra el boss
      if (!boss.isDestruint() && Utils.hiHaColisio(jugador.getPosicio(), jugador.getTamany(), boss.getPosicio(), boss.getAmple())) {
        jugador.rebreDany(30); // Dany massiu de contacte
      }

      // Col·lisió del Kamehameha del boss contra el jugador (desintegrador!)
      if (boss.isDisparantKamehameha()) {
        float limitX = boss.getPosicio().x - 40;
        if (jugador.getPosicio().x - jugador.getTamany()/2.0f <= limitX) {
          float radiCollisioY = jugador.getTamany()/2.0f + 48.0f; // Amplada del feix + radi de la nau
          if (Math.abs(jugador.getPosicio().y - boss.getKamehamehaY()) < radiCollisioY) {
            int danyLaser = (configJSON.getInt("dificultat") == 1) ? 2 : 1;
            jugador.rebreDany(danyLaser); // Dany continu per frame (Dificil = 2, Normal = 1)
          }
        }
      }

      // Transició a la victòria quan s'ha acabat d'explotar el boss
      if (boss.haAcabatExplosio()) {
        estatJoc = 3;
        tempsTransicio = millis();
      }
    }

    // ==========================================
    // 4. BALES PLAYER (CONTRA TOT!)
    // ==========================================
    for (int j = jugador.getDisparos().size() - 1; j >= 0; j--) {
      Dispar balaTeua = jugador.getDisparos().get(j);
      boolean balaHaXocat = false;

      // 4.1 Contra Enemics
      for (int i = llistaEnemics.size() - 1; i >= 0; i--) {
        Enemic e = llistaEnemics.get(i);
        if (!e.isDestruint() && Utils.hiHaColisio(balaTeua.getPosicio(), balaTeua.getTamany(), e.getPosicio(), e.getTamany())) {
          e.rebreDany(10);
          balaHaXocat = true;
          if (e.estaDestruit()) {
            incrementarPunts(100);
          }
          break;
        }
      }

      // 4.2 Contra Meteorits
      if (!balaHaXocat) {
        for (int i = llistaMeteorits.size() - 1; i >= 0; i--) {
          Meteorit m = llistaMeteorits.get(i);
          if (!m.isDestruint() && Utils.hiHaColisio(balaTeua.getPosicio(), balaTeua.getTamany(), m.getPosicio(), m.getTamany())) {
            m.rebreDany(10);
            balaHaXocat = true;
            if (m.estaDestruit()) {
              incrementarPunts(20);
            }
            break;
          }
        }
      }

      // 4.3 Contra Mines
      if (!balaHaXocat) {
        for (int i = llistaMines.size() - 1; i >= 0; i--) {
          Mina m = llistaMines.get(i);
          if (!m.isExplotant() && Utils.hiHaColisio(balaTeua.getPosicio(), balaTeua.getTamany(), m.getPosicio(), m.getTamany())) {
            balaHaXocat = true;
            m.rebreDany(10); // Restem 10 als 20 HP de la mina
            break;
          }
        }
      }

      // 4.4 Contra el Jefe Final (Monstre Final)
      if (!balaHaXocat && boss != null && boss.isActiu() && !boss.isDestruint()) {
        if (Utils.hiHaColisio(balaTeua.getPosicio(), balaTeua.getTamany(), boss.getPosicio(), boss.getAmple())) {
          boss.rebreDany(10);
          balaHaXocat = true;
          if (boss.estaDestruit()) {
            incrementarPunts(2000); // Gran bonificació
          }
        }
      }

      if (balaHaXocat) {
        jugador.getDisparos().remove(j);
      }
    } // <--- FÍ DEL BUCLE DE LES BALES

    // ==========================================
    // 5. COMPROVAR SI HEM MORT (Ara sí, fora del bucle de les bales!)
    // ==========================================
    if (jugador.getVida() <= 0) {
      estatJoc = 2; // Passem a la pantalla de Game Over
    }
  } else if (estatJoc == 1) {
    // ==========================================
    // ESTAT 1: TRANSICIÓ DE NIVELL REDISSENYADA
    // ==========================================
    nivellActual.dibuixarFons(configJSON.getBoolean("desactivarParallax"));
    fill(0, 0, 40, 210); // Fons blavós fosc i transparent per donar profunditat
    rect(0, 0, width, height);

    fill(0, 255, 100); // Verd neó
    textAlign(CENTER, CENTER);
    textSize(60);
    String txtSuperat = idiomaActual.equals("cat") ? "NIVELL " + numeroNivell + " SUPERAT!" : "LEVEL " + numeroNivell + " CLEARED!";
    text(txtSuperat, width/2, height/2 - 60);

    fill(200, 200, 255);
    textSize(25);
    // Animació visual dels punts suspensius usant frameCount
    String punts = "";
    if (frameCount % 60 > 45) punts = "...";
    else if (frameCount % 60 > 30) punts = "..";
    else if (frameCount % 60 > 15) punts = ".";
    text(getTraduccio("preparant") + (numeroNivell + 1) + punts, width/2, height/2 + 20);

    // Barra de progrés visual
    float progresCarga = constrain((millis() - tempsTransicio) / 3000.0f, 0, 1);
    fill(50);
    rect(width/2 - 150, height/2 + 80, 300, 10, 5);
    fill(0, 255, 100);
    rect(width/2 - 150, height/2 + 80, 300 * progresCarga, 10, 5);

    if (millis() - tempsTransicio > 3000) {
      numeroNivell++;
      carregarNivell(numeroNivell);
      estatJoc = 0;
    }
  } else if (estatJoc == 2) {
    // ==========================================
    // ESTAT 2: GAME OVER REDISSENYAT
    // ==========================================
    nivellActual.dibuixarFons(configJSON.getBoolean("desactivarParallax"));
    fill(15, 0, 0, 220); // Fons molt fosc, tocant a negre per a major dramatisme
    rect(0, 0, width, height);

    fill(255, 50, 50); // Roig agressiu
    textAlign(CENTER, CENTER);
    textSize(80);
    text("GAME OVER", width/2, height/2 - 40);

    // Text parpadejant
    if (frameCount % 60 < 30) {
      fill(255, 255, 0);
      textSize(22);
      text(getTraduccio("tornarMenu"), width/2, height/2 + 60);
    }
  } else if (estatJoc == 3) {
      // ==========================================
      // ESTAT 3: PANTALLA DE VICTÒRIA (JOC COMPLETAT)
      // ==========================================
      nivellActual.dibuixarFons(configJSON.getBoolean("desactivarParallax"));
      fill(0, 40, 20, 200); // Fons verdós transparent molt premium
      rect(0, 0, width, height);

      textAlign(CENTER, CENTER);
      fill(0, 255, 128); // Verd mar brillant
      textSize(80);
      text(idiomaActual.equals("cat") ? "VICTÒRIA!" : "VICTORY!", width/2, height/2 - 70);

      fill(255);
      textSize(26);
      text(getTraduccio("felicitats"), width/2, height/2 + 10);
      
      fill(0, 255, 255);
      textSize(22);
      text(getTraduccio("puntuacioFinal") + marcador.getScore(), width/2, height/2 + 60);

      // Text de reinici parpadejant
      if (frameCount % 60 < 30) {
        fill(255, 255, 0);
        textSize(18);
        text(getTraduccio("tornarMenu"), width/2, height/2 + 120);
      }
    }
}

// ==========================================
// FUNCIONS AUXILIARS I METODES D'INICI
// ==========================================

void generarEnemics() {
  if (framesIntervalSpawn <= 0) return;

  int interval = framesIntervalSpawn;
  if (configJSON.getInt("dificultat") == 0) {
    interval = (int)(framesIntervalSpawn * 1.5f); // Normal: enemics triguen 1.5x en aparèixer
  }

  if (contadorFramesNivell > 0 && contadorFramesNivell % interval == 0) {
    int atzar = (int)random(0, 3);

    // Ajustem la IA/Probabilitat segons el nivell i la narrativa
    if (numeroNivell == 3) atzar = (int)random(0, 2); // N.3: Kamikazes i Interceptors (sense miners)
    else if (numeroNivell == 6) {
      // N.6: Esquadró d'assalt ràpid (molts Kamikazes, algun Interceptor, zero miners)
      atzar = random(1) < 0.6f ? 0 : 1;
    }

    if (atzar == 0) llistaEnemics.add(new Kamikaze(jugador.getPosicio()));
    else if (atzar == 1) llistaEnemics.add(new Interceptor(jugador.getPosicio()));
    else llistaEnemics.add(new Miner(jugador.getPosicio()));
  }
}

void generarBoosters() {
  // Eix un Booster aleatori cada 10 segons
  if (contadorFramesNivell > 0 && contadorFramesNivell % 600 == 0) {
    int atzar = (int)random(0, 3);

    if (atzar == 0) llistaBoosters.add(new BoosterVida());
    else if (atzar == 1) llistaBoosters.add(new BoosterAtac());
    else llistaBoosters.add(new BoosterEscut());
  }
}

void generarMeteorits() {
  if (framesIntervalMeteorits > 0) {
    int interval = framesIntervalMeteorits;
    if (configJSON.getInt("dificultat") == 0) {
      interval = (int)(framesIntervalMeteorits * 1.5f); // Normal: meteorits triguen 1.5x en aparèixer
    }
    if (contadorFramesNivell % interval == 0) {
      int tamanyAleatori = (int)random(20, 65); // Mida aleatòria
      int danyAleatori = tamanyAleatori / 2;    // El dany és proporcional a la mida
      llistaMeteorits.add(new Meteorit(tamanyAleatori, danyAleatori));
    }
  }
}

void keyPressed() {
  if (key == ESC) {
    key = 0; // Evita tancar el sketch en Processing
    if (estatJoc == 0) {
      enPausa = !enPausa;
      if (enPausa) {
        jugador.setMoureAmunt(false);
        jugador.setMoureAvall(false);
        jugador.setMoureEsquerra(false);
        jugador.setMoureDreta(false);
        jugador.setDisparant(false);
      }
      actualitzarVisibilitatMenus();
    }
    return;
  }

  if (enPausa) return;

  if (keyCode == UP || key == 'w' || key == 'W')    jugador.setMoureAmunt(true);
  if (keyCode == DOWN || key == 's' || key == 'S')  jugador.setMoureAvall(true);
  if (keyCode == LEFT || key == 'a' || key == 'A')  jugador.setMoureEsquerra(true);
  if (keyCode == RIGHT || key == 'd' || key == 'D') jugador.setMoureDreta(true);

  // Assegurem que l'espai funciona comprovant explícitament el keyCode 32
  if (key == ' ' || keyCode == 32 || key == 'x' || key == 'X' || key == 'l' || key == 'L') jugador.setDisparant(true);

  // Si estem morts o hem guanyat i polsem R, reiniciem el joc anant al menú
  if ((estatJoc == 2 || estatJoc == 3) && (key == 'r' || key == 'R')) {
    estatJoc = -1;
    enConfiguracio = false;
    enPausa = false;
    actualitzarVisibilitatMenus();
    controles.show();

    carregarConfiguracio();
    jugador.aplicarConfiguracio(100, 5, 0);
  }
}

void keyReleased() {
  if (enPausa) return;

  if (keyCode == UP || key == 'w' || key == 'W')    jugador.setMoureAmunt(false);
  if (keyCode == DOWN || key == 's' || key == 'S')  jugador.setMoureAvall(false);
  if (keyCode == LEFT || key == 'a' || key == 'A')  jugador.setMoureEsquerra(false);
  if (keyCode == RIGHT || key == 'd' || key == 'D') jugador.setMoureDreta(false);

  if (key == ' ' || keyCode == 32 || key == 'x' || key == 'X' || key == 'l' || key == 'L') jugador.setDisparant(false);
}

void carregarNivell(int num) {
  llistaEnemics.clear();
  llistaMeteorits.clear();
  llistaBoosters.clear();
  balesEnemigues.clear();
  llistaMines.clear();
  jugador.getDisparos().clear();
  boss = null; // Reiniciem el boss
  contadorFramesNivell = 0;

  // Agafem els punts actuals per sumar-los a l'objectiu del nivell
  int puntsBase = marcador.getScore();

  if (num == 1) {
    nivellActual = new Pantalla(this, 1, getNomNivell(1), "./img/lvl1.png", puntsBase + 500, 2500, 0, 0);
  } else if (num == 2) {
    // NIVELL 2: L'únic nivell 100% de temps i meteorits (esquivar 20 segons)
    nivellActual = new Pantalla(this, 2, getNomNivell(2), "./img/lvl2.png", 99999, 0, 6, 20);
  } else if (num == 3) {
    nivellActual = new Pantalla(this, 3, getNomNivell(3), "./img/lvl3.png", puntsBase + 800, 2000, 1, 0);
  } else if (num == 4) {
    // NIVELL 4: Ara és per punts. Viatge amb alguns meteorits i enemics.
    nivellActual = new Pantalla(this, 4, getNomNivell(4), "./img/lvl4.png", puntsBase + 1200, 1500, 2, 0);
  } else if (num == 5) {
    nivellActual = new Pantalla(this, 5, getNomNivell(5), "./img/lvl5.png", puntsBase + 1500, 1200, 0, 0);
  } else if (num == 6) {
    nivellActual = new Pantalla(this, 6, getNomNivell(6), "./img/lvl6.png", puntsBase + 1800, 1000, 1, 0);
  } else if (num == 7) {
    nivellActual = new Pantalla(this, 7, getNomNivell(7), "./img/lvl7.png", puntsBase + 2200, 800, 0, 0);
  } else if (num == 8) {
    // NIVELL 8: Ara és per punts.
    nivellActual = new Pantalla(this, 8, getNomNivell(8), "./img/lvl8.png", puntsBase + 2600, 800, 2, 0);
  } else if (num == 9) {
    nivellActual = new Pantalla(this, 9, getNomNivell(9), "./img/lvl9.png", puntsBase + 3000, 600, 1, 0);
  } else if (num == 10) {
    // NIVELL 10: El Monstre Final (Jefe de Proxima Centauri)
    // Utilitzem el fons del nivell 9 amb un objectiu molt gran que requereix matar el boss
    nivellActual = new Pantalla(this, 10, getNomNivell(10), "./img/lvl9.png", 99999, 0, 0, 0);
    boss = new MonstreFinal(10, configJSON.getInt("dificultat") == 1);
  } else {
    println("PREPARAT PEL BOSS!");
    exit();
  }
  intervalSpawn = nivellActual.getVelocitatSpawn();

  // NOU: Pre-calculem els intervals de frames per optimització
  framesIntervalSpawn = (intervalSpawn > 0) ? (int)((intervalSpawn / 1000.0f) * 60) : 0;
  if (framesIntervalSpawn <= 0 && intervalSpawn > 0) framesIntervalSpawn = 60;

  int freqMet = nivellActual.getNumMeteorits();
  framesIntervalMeteorits = (freqMet > 0) ? (60 / freqMet) : 0;
}

public void iniciarJoc() {
  enConfiguracio = false;
  controles.hide();
  carregarConfiguracio();
  jugador.resetJugador();
  marcador.resetScore();
  
  jugador.aplicarConfiguracio(100, 5, 0);
  
  estatJoc = 0;
  numeroNivell = 1; // Comença des del nivell 1
  carregarNivell(numeroNivell);
}

public void iniciarBoss() {
  enConfiguracio = false;
  controles.hide();
  carregarConfiguracio();
  jugador.resetJugador();
  marcador.resetScore();
  
  jugador.aplicarConfiguracio(100, 5, 0);
  
  estatJoc = 0;
  numeroNivell = 10; // Comença directament al nivell del boss
  carregarNivell(numeroNivell);
}

void guardarPartida() {
  JSONObject saveJSON = new JSONObject();
  saveJSON.setInt("nivell", numeroNivell);
  saveJSON.setInt("punts", marcador.getScore());
  saveJSONObject(saveJSON, "data/partida.json");
}

public void carregarPartida() {
  JSONObject saveJSON = loadJSONObject("data/partida.json");
  if (saveJSON != null) {
    int lvl = saveJSON.getInt("nivell");
    int pts = saveJSON.getInt("punts");
    
    enPausa = false;
    controles.hide();
    carregarConfiguracio();
    jugador.resetJugador();
    marcador.resetScore();
    
    marcador.incrementScore(pts); 
    jugador.aplicarConfiguracio(100, 5, 0);
    
    estatJoc = 0;
    numeroNivell = lvl;
    carregarNivell(numeroNivell);
    
    actualitzarVisibilitatMenus();
  }
}

public void resumirJoc() {
  enPausa = false;
  actualitzarVisibilitatMenus();
}

public void guardarIEixir() {
  guardarPartida();
  enPausa = false;
  estatJoc = -1;
  controles.show();
  actualitzarVisibilitatMenus();
}

void carregarConfiguracio() {
  configJSON = loadJSONObject("data/config.json");
  if (configJSON == null) {
    configJSON = new JSONObject();
  }
  if (!configJSON.hasKey("dificultat")) {
    configJSON.setInt("dificultat", 0); 
  }
  if (!configJSON.hasKey("desactivarParallax")) {
    configJSON.setBoolean("desactivarParallax", false);
  }
  if (!configJSON.hasKey("mostrarFPS")) {
    configJSON.setBoolean("mostrarFPS", false);
  }
}

public void canviarDificultat(boolean value) {
  configJSON.setInt("dificultat", value ? 1 : 0);
  saveJSONObject(configJSON, "data/config.json");
}

public void canviarParallax(boolean value) {
  configJSON.setBoolean("desactivarParallax", value);
  saveJSONObject(configJSON, "data/config.json");
}

public void canviarFPS(boolean value) {
  configJSON.setBoolean("mostrarFPS", value);
  saveJSONObject(configJSON, "data/config.json");
}

public void desplegableIdioma(float n) {
  int idx = (int) n;
  String targetIdioma = (idx == 0) ? "cat" : "eng";
  if (!idiomaActual.equals(targetIdioma)) {
    idiomaActual = targetIdioma;
    aplicarIdioma();
    actualitzarVisibilitatMenus();
  }
}

public void obrirConfiguracio() {
  enConfiguracio = true;
  actualitzarVisibilitatMenus();
}

public void tornarAlMenu() {
  enConfiguracio = false;
  actualitzarVisibilitatMenus();
}

public void actualitzarVisibilitatMenus() {
  if (controles != null) {
    controles.actualitzarVisibilitatMenus();
  }
}

void aplicarIdioma() {
  if (controles != null) {
    controles.aplicarIdioma();
  }
}

String getTraduccio(String clau) {
  try {
    XML xmlSencer = loadXML("data/idiomes.xml");
    XML xmlIdioma = xmlSencer.getChild(idiomaActual);
    XML xmlClau = xmlIdioma.getChild(clau);
    if (xmlClau != null) {
      return xmlClau.getContent();
    }
  } catch (Exception e) {
    println("Error loading translation for: " + clau);
  }
  return "";
}

String getNomNivell(int num) {
  return getTraduccio("lvl" + num);
}

void incrementarPunts(int valor) {
  if (configJSON != null && configJSON.getInt("dificultat") == 1) {
    marcador.incrementScore((int)(valor * 1.5f));
  } else {
    marcador.incrementScore(valor);
  }
}

