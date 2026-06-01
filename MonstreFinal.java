import processing.core.PApplet;
import processing.core.PVector;

public class MonstreFinal {

  // ATRIBUTS
  private PVector posicio;
  private int vida;
  private int vidaMaxima;
  private int ample;
  private int alt;
  private boolean actiu;

  // ATRIBUTS DE COMBAT I MOVIMENT
  private int temporizadorDispar;
  private int cooldownDispar;
  private float angleOscilacio;
  private boolean destruint;
  private Animation animacioExplosio;

  // CONSTRUCTORS

  // Constructor per defecte
  public MonstreFinal() {
    this.posicio = new PVector(900, 300); // Neix fora de pantalla a la dreta
    this.vida = 800; // Salut inicial consistent per a un cap de combat
    this.vidaMaxima = 800;
    this.ample = 120; // Tamany imponent (abans 100)
    this.alt = 120;
    this.actiu = true;
    
    this.temporizadorDispar = 0;
    this.cooldownDispar = 60; // Ataca cada 1 segon (60 frames a 60 FPS)
    this.angleOscilacio = 0;
    this.destruint = false;
  }

  // Constructor parametritzat
  public MonstreFinal(int nivell) {
    this();
    this.vida = 800 + (nivell - 10) * 200; // Escalat de vida opcional si hi ha variació
    this.vidaMaxima = this.vida;
  }

  // METODES
  public void actualitzar() {
    if (this.destruint) {
      if (this.animacioExplosio != null) {
        this.animacioExplosio.update();
      }
      // Durant la destrucció, frena i flota lentament cap a darrere
      this.posicio.x += 0.25f;
      return;
    }

    if (!this.actiu) return;

    // 1. MOVIMENT DE PRESENTACIÓ: Avança cap a la seua posició fins x = 620
    if (this.posicio.x > 620) {
      this.posicio.x -= 2.0f;
    } else {
      // 2. MOVIMENT DE COMBAT: Oscil·lació vertical sinusoidal per esquivar i ser dinàmic
      this.angleOscilacio += 0.025f; // Velocitat d'oscil·lació
      this.posicio.y = 300.0f + (float)Math.sin(this.angleOscilacio) * 160.0f; // Oscil·la entre 140 i 460
    }
  }

  public void mostrar(PApplet app) {
    if (!this.actiu) return;

    if (this.destruint) {
      if (this.animacioExplosio == null) {
        // Spritesheet de 256x256 en quadrícula 4x4 -> 16 frames de 64x64
        this.animacioExplosio = new Animation(app, "Explosio", "./img/explosion.png", 64, 64, 4, 4, 0);
        this.animacioExplosio.setLoop(false);
        this.animacioExplosio.setDelay(4); // Explosió més lenta i dramàtica
      }
      // Dibuixem l'explosió gegant sobre el boss (escala 4.5f -> 288 píxels de diàmetre)
      this.animacioExplosio.display(this.posicio, 1, 4.5f);
      return;
    }

    // -------------------------------------------------------------
    // DIBUIX DEL BOSS (Procedural neó de gran qualitat)
    // -------------------------------------------------------------
    app.pushMatrix();
    app.translate(this.posicio.x, this.posicio.y);
    app.rectMode(PApplet.CENTER);
    
    // 1. FLAMES PROPULSORS (Motors del darrere - flameig dinàmic amb frameCount)
    app.fill(255, 60, 0, 160 + (app.frameCount % 6) * 15);
    app.noStroke();
    app.triangle(this.ample / 2.0f - 10, -35, this.ample / 2.0f + 25, -35, this.ample / 2.0f - 10, -25);
    app.triangle(this.ample / 2.0f - 10, 35, this.ample / 2.0f + 25, 35, this.ample / 2.0f - 10, 25);
    app.fill(255, 180, 0, 180 + (app.frameCount % 4) * 20);
    app.triangle(this.ample / 2.0f - 5, -10, this.ample / 2.0f + 40, 0, this.ample / 2.0f - 5, 10);
    
    // 2. ALES EXTERIORS (Metàl·liques amb contrast)
    app.fill(50, 50, 70);
    app.stroke(0, 180, 255); // Vora blau elèctric
    app.strokeWeight(3);
    
    app.beginShape();
    app.vertex(this.ample / 2.0f, -20);
    app.vertex(20, -this.alt / 2.0f);
    app.vertex(-this.ample / 2.0f + 15, -this.alt / 2.0f + 15);
    app.vertex(-20, -20);
    app.endShape(PApplet.CLOSE);
    
    app.beginShape();
    app.vertex(this.ample / 2.0f, 20);
    app.vertex(20, this.alt / 2.0f);
    app.vertex(-this.ample / 2.0f + 15, this.alt / 2.0f - 15);
    app.vertex(-20, 20);
    app.endShape(PApplet.CLOSE);
    
    // 3. FUSELLATGE CENTRAL (Blindatge fosc)
    app.fill(30, 30, 40);
    app.stroke(255, 0, 100); // Vora vermell neó
    app.beginShape();
    app.vertex(this.ample / 2.0f - 15, 0);
    app.vertex(15, -30);
    app.vertex(-this.ample / 2.0f + 20, -20);
    app.vertex(-this.ample / 2.0f, 0);
    app.vertex(-this.ample / 2.0f + 20, 20);
    app.vertex(15, 30);
    app.endShape(PApplet.CLOSE);
    
    // 4. CABINA ENVIADA (Vermell intens de combat)
    app.fill(255, 0, 50);
    app.noStroke();
    app.triangle(-this.ample / 2.0f + 15, 0, -15, -12, -15, 12);
    
    // 5. NUCLIS D'ENERGIA (Pulsants en verd neó / cian)
    if (app.frameCount % 24 < 12) {
      app.fill(0, 255, 200);
    } else {
      app.fill(0, 150, 180);
    }
    app.ellipse(5, -18, 14, 14);
    app.ellipse(5, 18, 14, 14);
    
    app.popMatrix();

    // -------------------------------------------------------------
    // DIBUIX DE LA BARRA DE VIDA DEL JEFE (HUD INFERIOR PANTALLA)
    // -------------------------------------------------------------
    app.pushStyle();
    
    // Contenidor HUD fons fosc
    app.fill(0, 0, 10, 180);
    app.noStroke();
    app.rectMode(PApplet.CENTER);
    app.rect(app.width / 2, app.height - 40, 520, 34, 10);
    
    // Fons interior vermell apagadíssim
    app.rectMode(PApplet.CORNER);
    app.fill(60, 10, 10);
    app.rect(app.width / 2 - 250, app.height - 50, 500, 20, 5);
    
    // Barra activa vermell neó brillant
    app.fill(255, 20, 60);
    float ampleVida = PApplet.map(Math.max(0, this.vida), 0, this.vidaMaxima, 0, 500);
    app.rect(app.width / 2 - 250, app.height - 50, ampleVida, 20, 5);
    
    // Etiqueta del Boss
    app.fill(255);
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.textSize(13);
    app.text("PROXIMA CENTAURI GUARDIAN (BOSS)", app.width / 2, app.height - 41);
    
    app.popStyle();
  }

  // Lógica per llançar atacs al jugador
  public java.util.ArrayList<Dispar> disparar(PVector posicioJugador) {
    java.util.ArrayList<Dispar> nousDispars = new java.util.ArrayList<Dispar>();
    if (this.destruint || !this.actiu) return nousDispars;

    // Només ataca si ja s'ha col·locat en posició de combat
    if (this.posicio.x <= 620) {
      this.temporizadorDispar++;
      if (this.temporizadorDispar >= this.cooldownDispar) {
        this.temporizadorDispar = 0;

        double atzar = Math.random();
        if (atzar < 0.45) {
          // Atac 1: Dispar de ventall triple (un recte, un cap amunt i un cap avall)
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y - 20), new PVector(0, this.posicio.y - 120), true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y + 20), new PVector(0, this.posicio.y + 120), true));
        } else {
          // Atac 2: Ràfega de 2 bales ràpides cap a la posició real de la nau del jugador
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y - 25), posicioJugador, true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y + 25), posicioJugador, true));
        }
      }
    }
    return nousDispars;
  }

  public void rebreDany(int dany) {
    if (this.destruint) return;
    this.vida -= dany;
    if (this.vida <= 0) {
      this.vida = 0;
      this.destruint = true;
    }
  }

  public boolean estaDestruit() {
    return this.vida <= 0;
  }

  public boolean isDestruint() {
    return this.destruint;
  }

  public boolean haAcabatExplosio() {
    return this.destruint && this.animacioExplosio != null && this.animacioExplosio.hasFinished();
  }

  public PVector getPosicio() {
    return this.posicio;
  }

  public int getAmple() {
    return this.ample;
  }

  public int getAlt() {
    return this.alt;
  }

  public boolean isActiu() {
    return this.actiu;
  }

  public void setActiu(boolean b) {
    this.actiu = b;
  }
}
