import processing.core.PApplet;
import processing.core.PImage;

public class Pantalla {

  // ATRIBUTS
  private PApplet app;
  private int idNivell;
  private String titol;
  private PImage fons;
  private int duradaSegons;

  // REGLES DEL NIVELL
  private int objectiuPunts;
  private int velocitatSpawnEnemics; // Milisegons entre enemics
  private int numMeteoritsInicials;

  // VARIABLES PER AL PARALLAX
  private float offsetFons = 0;
  private float[] starX = new float[100];
  private float[] starY = new float[100];
  private float[] starSpeed = new float[100];
  private float[] starAlpha = new float[100]; // NOU: Pre-calculat per a optimització

  public Pantalla(PApplet app, int id, String titol, String rutaFons, int objPunts, int velSpawn, int numMeteorits, int durada) {
    this.app = app;
    this.idNivell = id;
    this.titol = titol;
    if (rutaFons != null && !rutaFons.equals("")) {
      this.fons = app.loadImage(rutaFons);
    }
    this.objectiuPunts = objPunts;
    this.velocitatSpawnEnemics = velSpawn;
    this.numMeteoritsInicials = numMeteorits;
    this.duradaSegons = durada; // 0 si es por puntos, mas de 0 si es por tiempo
    
    // Inicialitzem la capa d'estreles del Parallax
    for(int i = 0; i < 100; i++) {
      starX[i] = app.random(app.width);
      starY[i] = app.random(app.height);
      starSpeed[i] = app.random(1f, 4f); // Velocitats/tamanys aleatoris entre 1 i 4
      starAlpha[i] = app.map(starSpeed[i], 1f, 4f, 100f, 255f); // NOU: Pre-calculat
    }
  }

  // Mètode per dibuixar el fons i el títol
  public void dibuixarFons() {
    dibuixarFons(false);
  }

  public void dibuixarFons(boolean desactivarParallax) {
    // Forcem els modes a CORNER per evitar bugs visuals heretats d'altres objectes (1/4 de pantalla)
    app.imageMode(PApplet.CORNER);
    app.rectMode(PApplet.CORNER);
    
    // 1. CAPA BASE: Imatge desplaçant-se o fixa segons el nivell
    boolean fonsFix = (this.idNivell == 3);
    
    if (fonsFix) {
      if (this.fons != null) {
        // Calculem l'ample proporcional a l'alçada per no deformar la imatge
        float proporcio = (float) this.fons.width / this.fons.height;
        float ampleImatge = app.height * proporcio;
        float offsetX = (app.width - ampleImatge) / 2; // La centrem horitzontalment
        app.image(this.fons, offsetX, 0, ampleImatge, app.height);
      } else {
        app.background(0);
      }
    } else {
      float ampleImatge = app.width; 
      if (this.fons != null) {
        float proporcio = (float) this.fons.width / this.fons.height;
        ampleImatge = app.height * proporcio;
      }
  
      if (!desactivarParallax) {
        offsetFons -= 0.5f; 
      }
      if (offsetFons <= -ampleImatge) offsetFons += ampleImatge; // Bucle infinit basat en l'ample real
      
      if (this.fons != null) {
        app.image(this.fons, offsetFons, 0, ampleImatge, app.height);
        app.image(this.fons, offsetFons + ampleImatge, 0, ampleImatge, app.height); // La segona imatge enganxada darrere
      } else {
        app.background(0);
      }
    }

    // 2. CAPA SUPERIOR: Estreles procedurales per donar efecte 3D
    app.pushStyle();
    app.noStroke();
    for (int i = 0; i < 100; i++) {
      app.fill(255, 255, 255, starAlpha[i]); // Les més ràpides brillen més (Pre-calculat)
      app.ellipse(starX[i], starY[i], starSpeed[i], starSpeed[i]);
      
      if (!desactivarParallax) {
        starX[i] -= starSpeed[i] * 1.5f; // Es mouen cap a l'esquerra segons la seua velocitat
        if (starX[i] < 0) { starX[i] = app.width; starY[i] = app.random(app.height); } // Reapareixen per la dreta
      }
    }
    app.popStyle();

    // 3. CAPA DE CONTRAST: Filtre semi-transparent per enfosquir el fons i fer destacar les entitats (nau, enemics, meteorits i bales)
    app.pushStyle();
    app.fill(0, 0, 0, 110); // Negre amb opacitat de 110 (sobre 255)
    app.noStroke();
    app.rectMode(PApplet.CORNER);
    app.rect(0, 0, app.width, app.height);
    app.popStyle();

    // Dibuixem el titol del nivell dalt a l'esquerra
    app.fill(255);
    app.textSize(18);
    app.textAlign(PApplet.LEFT);
    String currentIdioma = ((RescatProxima)app).idiomaActual;
    String prefix = currentIdioma.equals("cat") ? "Nivell " : (currentIdioma.equals("esp") ? "Nivel " : "Level ");
    app.text(prefix + this.idNivell + " - " + this.titol, 10, 75); // Baixem l'alçada a 75 perquè no xafe el HUD
    
    // Dibuixem l'objectiu del nivell a sota
    app.textSize(13);
    app.fill(200, 200, 255); // Color gris-blavós clar molt elegant
    String objText = "";
    if (this.idNivell == 10) {
      if (currentIdioma.equals("cat")) {
        objText = "Objectiu: Derrotar el Guardià";
      } else if (currentIdioma.equals("esp")) {
        objText = "Objetivo: Derrotar al Guardián";
      } else {
        objText = "Objective: Defeat the Guardian";
      }
    } else if (this.duradaSegons > 0) {
      if (currentIdioma.equals("cat")) {
        objText = "Objectiu: Aguantar " + this.duradaSegons + " segons";
      } else if (currentIdioma.equals("esp")) {
        objText = "Objetivo: Sobrevivir " + this.duradaSegons + " segundos";
      } else {
        objText = "Objective: Survive for " + this.duradaSegons + " seconds";
      }
    } else {
      if (currentIdioma.equals("cat")) {
        objText = "Objectiu: Arribar a " + this.objectiuPunts + " punts";
      } else if (currentIdioma.equals("esp")) {
        objText = "Objetivo: Llegar a " + this.objectiuPunts + " puntos";
      } else {
        objText = "Objective: Reach " + this.objectiuPunts + " points";
      }
    }
    app.text(objText, 10, 95);
  }

  // Comprova si el jugador ja ha arribat als punts necessaris per guanyar
  public boolean nivellSuperat(int puntsActuals) {
    return (puntsActuals >= this.objectiuPunts);
  }

  public int getVelocitatSpawn() {
    return this.velocitatSpawnEnemics;
  }
  public int getNumMeteorits() {
    return this.numMeteoritsInicials;
  }
  public int getDurada() {
    return this.duradaSegons;
  }
}
