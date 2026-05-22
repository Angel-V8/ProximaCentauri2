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
    }
  }

  // Mètode per dibuixar el fons i el títol
  public void dibuixarFons() {
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
  
      offsetFons -= 0.5f; 
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
      app.fill(255, 255, 255, app.map(starSpeed[i], 1, 4, 100, 255)); // Les més ràpides brillen més
      app.ellipse(starX[i], starY[i], starSpeed[i], starSpeed[i]);
      
      starX[i] -= starSpeed[i] * 1.5f; // Es mouen cap a l'esquerra segons la seua velocitat
      if (starX[i] < 0) { starX[i] = app.width; starY[i] = app.random(app.height); } // Reapareixen per la dreta
    }
    app.popStyle();

    // Dibuixem el titol del nivell dalt a l'esquerra
    app.fill(255);
    app.textSize(18);
    app.textAlign(PApplet.LEFT);
    app.text("Nivell " + this.idNivell + " - " + this.titol, 10, 75); // Baixem l'alçada a 75 perquè no xafe el HUD
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
