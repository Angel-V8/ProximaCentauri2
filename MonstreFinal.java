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
  private boolean destruint;
  private Animation animacioExplosio;

  // MOVIMENT NATURAL (Lliure de patrons rígids)
  private PVector targetPosicio;
  private int temporizadorNouTarget;

  // GESTIÓ D'ATACS RÀPIDS (Ràfegues)
  private int rafagaRestant;
  private int temporizadorRafaga;

  // ANIMACIÓ DE GOKU I VECTOR ZERO
  private Animation animacio;
  private PVector posicioZero; // PVector(0,0) per a renderitzar amb translat/scale in-place

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
    this.cooldownDispar = 60; // Ataca cada 1 segon per defecte
    this.destruint = false;

    this.targetPosicio = new PVector(620, 300);
    this.temporizadorNouTarget = 0;

    this.rafagaRestant = 0;
    this.temporizadorRafaga = 0;

    this.posicioZero = new PVector(0, 0);
  }

  // Constructor parametritzat
  public MonstreFinal(int nivell) {
    this();
    this.vida = 800 + (nivell - 10) * 200;
    this.vidaMaxima = this.vida;
  }

  // METODE AUXILIAR LERP (Per evitar dependència de PApplet fora del render)
  private float lerp(float start, float stop, float amt) {
    return start + (stop - start) * amt;
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
      // 2. MOVIMENT DE COMBAT FLUID: El cap tria un punt de destinació aleatori i hi vola de forma suau
      this.temporizadorNouTarget++;
      if (this.temporizadorNouTarget >= 120) { // Canvia de rumb cada 2 segons (120 frames)
        this.temporizadorNouTarget = 0;
        this.targetPosicio.x = (float)(Math.random() * 150) + 530; // X entre 530 i 680
        this.targetPosicio.y = (float)(Math.random() * 360) + 120; // Y entre 120 i 480
      }
      
      // Interpolació suau de posició
      this.posicio.x = lerp(this.posicio.x, this.targetPosicio.x, 0.025f);
      this.posicio.y = lerp(this.posicio.y, this.targetPosicio.y, 0.025f);
    }
  }

  public void mostrar(PApplet app) {
    if (!this.actiu) return;

    if (this.destruint) {
      if (this.animacioExplosio == null) {
        // Spritesheet de 256x256 en graella 4x4 -> 16 frames de 64x64
        this.animacioExplosio = new Animation(app, "Explosio", "./img/explosion.png", 64, 64, 4, 4, 0);
        this.animacioExplosio.setLoop(false);
        this.animacioExplosio.setDelay(4);
      }
      this.animacioExplosio.display(this.posicio, 1, 4.5f);
      return;
    }

    // Inicialització de l'animació de Goku (primera fila de goku.png, frames de 64x64)
    if (this.animacio == null) {
      this.animacio = new Animation(app, "Goku", "./img/goku.png", 64, 64, 4, 4, 1);
      this.animacio.setLoop(true);
      this.animacio.setDelay(8); // Velocitat de l'animació
    }

    // NOU RENDER FLUID: Efecte d'oscil·lació vertical procedural ('hover') per simular vol natural
    float hoverY = (float)Math.sin(app.frameCount * 0.08f) * 6.0f;

    app.pushMatrix();
    app.translate(this.posicio.x, this.posicio.y + hoverY);
    app.scale(-1, 1); // Voltem horitzontalment per fer que miri a l'esquerra (cap al jugador)
    float escala = (float)this.ample / 64.0f;
    this.animacio.display(this.posicioZero, 1, escala); // Dibuixat a (0,0) in-place
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

  // Lógica per llançar atacs al jugador (amb 4 patrons diferents)
  public java.util.ArrayList<Dispar> disparar(PVector posicioJugador) {
    java.util.ArrayList<Dispar> nousDispars = new java.util.ArrayList<Dispar>();
    if (this.destruint || !this.actiu) return nousDispars;

    // Només ataca si ja s'ha col·locat en posició de combat
    if (this.posicio.x <= 620) {
      
      // PATRÓ 1: Ràfega activa de Ki Blasts (Kamehameha Barrage)
      if (this.rafagaRestant > 0) {
        this.temporizadorRafaga++;
        if (this.temporizadorRafaga >= 8) { // Dispara un blast cada 8 frames
          this.temporizadorRafaga = 0;
          this.rafagaRestant--;
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), posicioJugador, true));
        }
        return nousDispars; // Continuem la ràfega sense executar altres atacs
      }

      this.temporizadorDispar++;
      if (this.temporizadorDispar >= this.cooldownDispar) {
        this.temporizadorDispar = 0;

        // Triem un dels 4 atacs de Goku a l'atzar
        int tipusAtac = (int)(Math.random() * 4);

        if (tipusAtac == 0) {
          // ATAC 0: Ventall triple de ràfega recte i oblic
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y - 20), new PVector(0, this.posicio.y - 120), true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y + 20), new PVector(0, this.posicio.y + 120), true));
        } 
        else if (tipusAtac == 1) {
          // ATAC 1: Doble blast ràpid directe al jugador
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y - 25), posicioJugador, true));
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y + 25), posicioJugador, true));
        } 
        else if (tipusAtac == 2) {
          // ATAC 2: Ràfega ràpida seqüencial (inici de Ki Blast Barrage)
          this.rafagaRestant = 5; // Llança 5 trets en total en els següents frames
          this.temporizadorRafaga = 0;
          nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), posicioJugador, true));
        } 
        else {
          // ATAC 3: Ona Radial/Espiral (6 bales simultànies en 360º)
          float anglePas = (float)(Math.PI * 2) / 6.0f;
          for (int i = 0; i < 6; i++) {
            float angle = i * anglePas;
            float targetX = this.posicio.x - 50 + (float)Math.cos(angle) * 200.0f;
            float targetY = this.posicio.y + (float)Math.sin(angle) * 200.0f;
            nousDispars.add(new Dispar(new PVector(this.posicio.x - 50, this.posicio.y), new PVector(targetX, targetY), true));
          }
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
