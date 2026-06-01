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

  // ANIMACIÓ DE GOKU I VECTOR ZERO
  private Animation animacio;
  private PVector posicioZero; // NOU: PVector(0,0) per a renderitzar amb translat/scale in-place

  // CONSTRUCTORS

  // Constructor per defecte
  public MonstreFinal() {
    this.posicio = new PVector(900, 300); // Neix fora de pantalla a la dreta
    this.vida = 800; // Salut inicial
    this.vidaMaxima = 800;
    this.ample = 120; // Mida de Goku en pantalla (120 píxels)
    this.alt = 120;
    this.actiu = true;
    
    this.temporizadorDispar = 0;
    this.cooldownDispar = 60; // Ataca cada 1 segon
    this.angleOscilacio = 0;
    this.destruint = false;
    this.posicioZero = new PVector(0, 0);
  }

  // Constructor parametritzat
  public MonstreFinal(int nivell) {
    this();
    this.vida = 800 + (nivell - 10) * 200;
    this.vidaMaxima = this.vida;
  }

  // METODES
  public void actualitzar() {
    if (this.destruint) {
      if (this.animacioExplosio != null) {
        this.animacioExplosio.update();
      }
      this.posicio.x += 0.25f;
      return;
    }

    if (!this.actiu) return;

    if (this.animacio != null) {
      this.animacio.update();
    }

    // 1. MOVIMENT DE PRESENTACIÓ: Avança cap a la seua posició fins x = 620
    if (this.posicio.x > 620) {
      this.posicio.x -= 2.0f;
    } else {
      // 2. MOVIMENT DE COMBAT: Oscil·lació vertical sinusoidal
      this.angleOscilacio += 0.025f;
      this.posicio.y = 300.0f + (float)Math.sin(this.angleOscilacio) * 160.0f;
    }
  }

  public void mostrar(PApplet app) {
    if (!this.actiu) return;

    if (this.destruint) {
      if (this.animacioExplosio == null) {
        this.animacioExplosio = new Animation(app, "Explosio", "./img/explosion.png", 64, 64, 4, 4, 0);
        this.animacioExplosio.setLoop(false);
        this.animacioExplosio.setDelay(4);
      }
      this.animacioExplosio.display(this.posicio, 1, 4.5f);
      return;
    }

    // Inicialització de la nova animació de Goku simplificada
    if (this.animacio == null) {
      // Carreguem la primera fila (quinaFila = 1) de goku.png de 256x256 en graella 4x4 (frames de 64x64)
      this.animacio = new Animation(app, "Goku", "./img/goku.png", 64, 64, 4, 4, 1);
      this.animacio.setLoop(true);
      this.animacio.setDelay(8); // Velocitat d'animació
    }

    // NOU RENDER: Evitem passar dir = -1 directament a la classe Animation (ja que la seua
    // implementació interna dibuixa en coordenades negatives causant invisibilitat).
    // En el seu lloc, girem tota la matriu al voltant de la posició del boss.
    app.pushMatrix();
    app.translate(this.posicio.x, this.posicio.y);
    app.scale(-1, 1); // Voltem horitzontalment per fer que miri a l'esquerra (cap al jugador)
    float escala = (float)this.ample / 64.0f;
    this.animacio.display(this.posicioZero, 1, escala); // Passem posició (0,0) i dir = 1
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
    app.text("PROXIMA CENTAURI GUARDIAN (GOKU BOSS)", app.width / 2, app.height - 41);
    
    app.popStyle();
  }

  public java.util.ArrayList<Dispar> disparar(PVector posicioJugador) {
    java.util.ArrayList<Dispar> nousDispars = new java.util.ArrayList<Dispar>();
    if (this.destruint || !this.actiu) return nousDispars;

    if (this.posicio.x <= 620) {
      this.temporizadorDispar++;
      if (this.temporizadorDispar >= this.cooldownDispar) {
        this.temporizadorDispar = 0;

        double atzar = Math.random();
        if (atzar < 0.45) {
          // Atac 1: Dispar de ventall triple
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y - 20), new PVector(0, this.posicio.y - 120), true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y + 20), new PVector(0, this.posicio.y + 120), true));
        } else {
          // Atac 2: Ràfega de 2 bales ràpides cap al jugador
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
