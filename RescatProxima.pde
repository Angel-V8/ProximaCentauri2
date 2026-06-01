import java.util.ArrayList;
import controlP5.*;

JSONObject configJSON;
ControlP5 cp5;

// Variables Globals
PImage fonsInici;
Pantalla nivellActual;
int contadorFramesNivell = 0;
int numeroNivell = 1; // Comença al nivell 1
int estatJoc = -1;
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

  // CONFIGURACIÓ DEL MENÚ
  cp5 = new ControlP5(this);

  cp5.addButton("btnJugar")
    .setLabel("INICIAR")
    .setPosition(width/2 - 100, height/2)
    .setSize(200, 50)
    .plugTo(this, "iniciarJoc");

  cp5.addButton("btnConfig")
    .setLabel("CONFIGURACIO")
    .setPosition(width/2 - 100, height/2 + 60)
    .setSize(200, 50)
    .plugTo(this, "obrirConfiguracio");

  cp5.addButton("btnBoss")
    .setLabel("JUGAR BOSS")
    .setPosition(width/2 - 100, height/2 + 120)
    .setSize(200, 50)
    .plugTo(this, "iniciarBoss");

  cp5.addScrollableList("desplegableIdioma")
    .setPosition(width/2 - 100, height/2 + 180)
    .setSize(200, 100)
    .setBarHeight(40)
    .setItemHeight(40)
    .addItem("Valencia", 0)
    .addItem("English", 1)
    .setLabel("Tria el teu idioma / Choose language")
    .close();

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
    fill(0, 255, 0);
    textAlign(CENTER, CENTER);
    textSize(60);
    text(textTitol, width/2, height/3 - 50);
  } else if (estatJoc == 0) {

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
    nivellActual.dibuixarFons();

    generarEnemics();
    generarBoosters();
    marcador.actualitza(contadorFramesNivell);
    marcador.mostra(jugador.getVida());

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

        // L'explosió danya enemics propers
        for (int j = llistaEnemics.size() - 1; j >= 0; j--) {
          Enemic e = llistaEnemics.get(j);
          if (!e.isDestruint() && Utils.hiHaColisio(e.getPosicio(), e.getTamany(), m.getPosicio(), m.getRadiExplosio() * 2)) {
            e.rebreDany(30); // Dany pesat per l'ona expansiva
            if (e.estaDestruit()) {
              marcador.incrementScore(100);
            }
          }
        }

        // L'explosió danya meteorits propers
        for (int j = llistaMeteorits.size() - 1; j >= 0; j--) {
          Meteorit met = llistaMeteorits.get(j);
          if (!met.isDestruint() && Utils.hiHaColisio(met.getPosicio(), met.getTamany(), m.getPosicio(), m.getRadiExplosio() * 2)) {
            met.rebreDany(30);
            if (met.estaDestruit()) {
              marcador.incrementScore(20);
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

        marcador.incrementScore(50);
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
            marcador.incrementScore(50);
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
            jugador.rebreDany(2); // Dany continu per frame (2 HP = desintegració en 50 frames ~ 0.8s)
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
            marcador.incrementScore(100);
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
              marcador.incrementScore(20);
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
            marcador.incrementScore(2000); // Gran bonificació
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
    nivellActual.dibuixarFons();
    fill(0, 0, 40, 210); // Fons blavós fosc i transparent per donar profunditat
    rect(0, 0, width, height);

    fill(0, 255, 100); // Verd neó
    textAlign(CENTER, CENTER);
    textSize(60);
    text("NIVELL " + numeroNivell + " SUPERAT!", width/2, height/2 - 60);

    fill(200, 200, 255);
    textSize(25);
    // Animació visual dels punts suspensius usant frameCount
    String punts = "";
    if (frameCount % 60 > 45) punts = "...";
    else if (frameCount % 60 > 30) punts = "..";
    else if (frameCount % 60 > 15) punts = ".";
    text("Preparant Nivell " + (numeroNivell + 1) + punts, width/2, height/2 + 20);

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
    nivellActual.dibuixarFons();
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
      text("Polsa 'R' per a tornar al Menú Principal", width/2, height/2 + 60);
    }
  } else if (estatJoc == 3) {
      // ==========================================
      // ESTAT 3: PANTALLA DE VICTÒRIA (JOC COMPLETAT)
      // ==========================================
      nivellActual.dibuixarFons();
      fill(0, 40, 20, 200); // Fons verdós transparent molt premium
      rect(0, 0, width, height);

      textAlign(CENTER, CENTER);
      fill(0, 255, 128); // Verd mar brillant
      textSize(80);
      text("VICTÒRIA!", width/2, height/2 - 70);

      fill(255);
      textSize(26);
      text("Felicitats! Has salvat Pròxima Centauri!", width/2, height/2 + 10);
      
      fill(0, 255, 255);
      textSize(22);
      text("PUNTUACIÓ FINAL: " + marcador.getScore(), width/2, height/2 + 60);

      // Text de reinici parpadejant
      if (frameCount % 60 < 30) {
        fill(255, 255, 0);
        textSize(18);
        text("Polsa 'R' per a tornar al Menú Principal", width/2, height/2 + 120);
      }
    }
}

// ==========================================
// FUNCIONS AUXILIARS I METODES D'INICI
// ==========================================

void generarEnemics() {
  if (framesIntervalSpawn <= 0) return;

  if (contadorFramesNivell > 0 && contadorFramesNivell % framesIntervalSpawn == 0) {
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
  if (framesIntervalMeteorits > 0 && contadorFramesNivell % framesIntervalMeteorits == 0) {
    // NOU: Meteorits de diferents tamanys i danys configurats dinàmicament
    int tamanyAleatori = (int)random(20, 65); // Mida aleatòria
    int danyAleatori = tamanyAleatori / 2;    // El dany és proporcional a la mida
    llistaMeteorits.add(new Meteorit(tamanyAleatori, danyAleatori));
  }
}

void keyPressed() {
  if (keyCode == UP)    jugador.setMoureAmunt(true);
  if (keyCode == DOWN)  jugador.setMoureAvall(true);
  if (keyCode == LEFT)  jugador.setMoureEsquerra(true);
  if (keyCode == RIGHT) jugador.setMoureDreta(true);

  // Assegurem que l'espai funciona comprovant explícitament el keyCode 32
  if (key == ' ' || keyCode == 32 || key == 'x' || key == 'X') jugador.setDisparant(true);

  // Si estem morts o hem guanyat i polsem R, reiniciem el joc anant al menú
  if ((estatJoc == 2 || estatJoc == 3) && (key == 'r' || key == 'R')) {
    estatJoc = -1;
    cp5.show();

    carregarConfiguracio();
    jugador.aplicarConfiguracio(configJSON.getInt("vida"), configJSON.getInt("velocitat"), configJSON.getInt("escut"));
  }
}

void keyReleased() {
  if (keyCode == UP)    jugador.setMoureAmunt(false);
  if (keyCode == DOWN)  jugador.setMoureAvall(false);
  if (keyCode == LEFT)  jugador.setMoureEsquerra(false);
  if (keyCode == RIGHT) jugador.setMoureDreta(false);

  if (key == ' ' || keyCode == 32 || key == 'x' || key == 'X') jugador.setDisparant(false);
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
    nivellActual = new Pantalla(this, 1, "Inici de l'expedició", "./img/lvl1.png", puntsBase + 500, 2500, 0, 0);
  } else if (num == 2) {
    // NIVELL 2: L'únic nivell 100% de temps i meteorits (esquivar 20 segons)
    nivellActual = new Pantalla(this, 2, "Cinturó de Júpiter", "./img/lvl2.png", 99999, 0, 6, 20);
  } else if (num == 3) {
    nivellActual = new Pantalla(this, 3, "Arribada a Plutó", "./img/lvl3.png", puntsBase + 800, 2000, 1, 0);
  } else if (num == 4) {
    // NIVELL 4: Ara és per punts. Viatge amb alguns meteorits i enemics.
    nivellActual = new Pantalla(this, 4, "Viatge interestel·lar", "./img/lvl4.png", puntsBase + 1200, 1500, 2, 0);
  } else if (num == 5) {
    nivellActual = new Pantalla(this, 5, "Arribada a Pròxima Centauri", "./img/lvl5.png", puntsBase + 1500, 1200, 0, 0);
  } else if (num == 6) {
    nivellActual = new Pantalla(this, 6, "Primera línia de protecció", "./img/lvl6.png", puntsBase + 1800, 1000, 1, 0);
  } else if (num == 7) {
    nivellActual = new Pantalla(this, 7, "Escut de defensa del planeta", "./img/lvl7.png", puntsBase + 2200, 800, 0, 0);
  } else if (num == 8) {
    // NIVELL 8: Ara és per punts.
    nivellActual = new Pantalla(this, 8, "Viatge a la base", "./img/lvl8.png", puntsBase + 2600, 800, 2, 0);
  } else if (num == 9) {
    nivellActual = new Pantalla(this, 9, "El rescat de la base", "./img/lvl9.png", puntsBase + 3000, 600, 1, 0);
  } else if (num == 10) {
    // NIVELL 10: El Monstre Final (Jefe de Proxima Centauri)
    // Utilitzem el fons del nivell 9 amb un objectiu molt gran que requereix matar el boss
    nivellActual = new Pantalla(this, 10, "El Guardià de Pròxima Centauri", "./img/lvl9.png", 99999, 0, 0, 0);
    boss = new MonstreFinal(10);
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
  cp5.hide();
  carregarConfiguracio();
  jugador.resetJugador();
  marcador.resetScore();
  jugador.aplicarConfiguracio(configJSON.getInt("vida"), configJSON.getInt("velocitat"), configJSON.getInt("escut"));
  estatJoc = 0;
  numeroNivell = 1; // Comença des del nivell 1
  carregarNivell(numeroNivell);
}

public void iniciarBoss() {
  cp5.hide();
  carregarConfiguracio();
  jugador.resetJugador();
  marcador.resetScore();
  jugador.aplicarConfiguracio(configJSON.getInt("vida"), configJSON.getInt("velocitat"), configJSON.getInt("escut"));
  estatJoc = 0;
  numeroNivell = 10; // Comença directament al nivell del boss
  carregarNivell(numeroNivell);
}

void carregarConfiguracio() {
  configJSON = loadJSONObject("data/config.json");
}

public void obrirConfiguracio() {
  launch(sketchPath("data/config.json"));
}

void aplicarIdioma() {
  try {
    XML xmlSencer = loadXML("data/idiomes.xml");
    XML xmlIdioma = xmlSencer.getChild(idiomaActual);

    textTitol = xmlIdioma.getChild("titol").getContent();
    String textIniciar = xmlIdioma.getChild("iniciar").getContent();
    String textConfig = xmlIdioma.getChild("config").getContent();
    String textBoss = xmlIdioma.getChild("boss") != null ? xmlIdioma.getChild("boss").getContent() : "JUGAR BOSS";

    if (cp5 != null) {
      cp5.get(controlP5.Button.class, "btnJugar").setLabel(textIniciar);
      cp5.get(controlP5.Button.class, "btnConfig").setLabel(textConfig);
      if (cp5.get(controlP5.Button.class, "btnBoss") != null) {
        cp5.get(controlP5.Button.class, "btnBoss").setLabel(textBoss);
      }
    }
  }
  catch (Exception e) {
    println("Error carregant idiomes.xml");
  }
}

public void desplegableIdioma(int n) {
  if (n == 0) idiomaActual = "cat";
  else if (n == 1) idiomaActual = "eng";
  aplicarIdioma();
}

