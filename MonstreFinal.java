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

  // ANIMACIONS DE GOKU
  private Animation animacioIdle;
  private Animation animacioAtac;
  private int temporizadorAtacVisual; // Controla quants frames es mostra Goku atacant

  // CONSTRUCTORS

  // Constructor per defecte
  public MonstreFinal() {
    this.posicio = new PVector(900, 300); // Neix fora de pantalla a la dreta
    this.vida = 800; // Salut inicial consistent per a un cap de combat
    this.vidaMaxima = 800;
    this.ample = 150; // Mida escalada per a fer a Goku imponent i visible
    this.alt = 150;
    this.actiu = true;
    
    this.temporizadorDispar = 0;
    this.cooldownDispar = 60; // Ataca cada 1 segon (60 frames a 60 FPS)
    this.angleOscilacio = 0;
    this.destruint = false;
    this.temporizadorAtacVisual = 0;
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

    // Actualitzar temporitzador d'animació d'atac
    if (this.temporizadorAtacVisual > 0) {
      this.temporizadorAtacVisual--;
    }

    // Actualitzar l'animació corresponent
    if (this.temporizadorAtacVisual > 0) {
      if (this.animacioAtac != null) this.animacioAtac.update();
    } else {
      if (this.animacioIdle != null) this.animacioIdle.update();
    }

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

    // Inicialització dels Spritesheets de Goku
    if (this.animacioIdle == null) {
      // Fila 1 de goku.png: Goku flotant/repaus
      this.animacioIdle = new Animation(app, "GokuIdle", "./img/goku.png", 512, 512, 2, 2, 1);
      this.animacioIdle.setLoop(true);
      this.animacioIdle.setDelay(12);
    }
    if (this.animacioAtac == null) {
      // Fila 2 de goku.png: Goku llançant Kamehameha
      this.animacioAtac = new Animation(app, "GokuAtac", "./img/goku.png", 512, 512, 2, 2, 2);
      this.animacioAtac.setLoop(true);
      this.animacioAtac.setDelay(6);
    }

    // Dibuix de Goku mirant cap a l'esquerra (-1 en la direcció)
    float escala = (float)this.ample / 512.0f;
    if (this.temporizadorAtacVisual > 0) {
      this.animacioAtac.display(this.posicio, -1, escala);
    } else {
      this.animacioIdle.display(this.posicio, -1, escala);
    }

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
    app.text("PROXIMA CENTAURI GUARDIAN (GOKU BOSS)", app.width / 2, app.height - 41);
    
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
        this.temporizadorAtacVisual = 25; // NOU: Activar posat d'atac durant 25 frames

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
