import processing.core.*;
import processing.data.*;
import java.util.ArrayList;

/************************************************************/
/*                                                          */
/*                Formatted Current Time                    */
/*                                                          */
/*************************************************************/
/*                        score                             */
/*************************************************************/
/*        lblPuntsA         *        lblPuntsB              */
/*************************************************************/
/*          puntsA          *          puntsB               */
/*************************************************************/
/*        lblExtra                Formatted CountDown       */
/*************************************************************/
/** 
 * La classe marcador és la classe que s'encarregara de portar el control del temps i mostrar-lo.
 * Aquesta classe disposarà de 2 comptadors A i B al quals se li assignarà una etiqueta: lblPuntsA o lblPuntsB
 * i un valor puntsA o puntsB que es motraràn a la pantalla principal de l'aplicació.
 * Els objectes de la classe marcador també disposaran d'un conjunt d'alarmes (Nom-Interaval) per que avise
 * a l'aplicació principal del pas de cert interval de temps, per exemple. Per tal d'implementar aquestes alarmes
 * utilitzarem un JSONArray encara que també podriem dissenyar la nostra classe Alarma amb tots els membres necessaris.
 */
public class Marcador {
  private PApplet   app;               /** Referència al PApplet principal */
  private PVector   position;          /** Posició en la que es dibuixarà el Marcador */
  private int       ample;             /** Ample en píxels del marcadors */
  private int       alt;               /** Alt en píxels del marcador */
  private int       currentTime;       /** Temps en milisegons actual (transcorregut des de la creació de l'objecte marcador) */
  private int       currentCountDown;  /** Temps de compte enrere en cas de voler utilitzar-se. Aquest atribut sol combinar-se amb countDownFlag */
  private int       countDownFlag;     /** Per controlar quan comencen i acaben els comptes enrere necessite un flag o inici del compte Enrere */
  private int       score;             /** Punts totals en cas d'haver-ne */
  private String    lblPuntsA;         /** Etiqueta que identifica el valor del primer marcador (ex. LOCAL) */
  private String    lblPuntsB;         /** Etiqueta que identifica el valor del segon marcador (ex. VISITANT) */
  private String    lblExtra;          /** Etiqueta extra. Podria identificar el temps extra **/
  private int       puntsB;            /** Comptador del primer marcador */
  private int       puntsA;            /** Comptador del segon marcador */
  private JSONArray alarmes;           /** Col·lecció amb les alarmes establides al marcador */

  /**
   * Crea un marcador per defecte de 200 píxels d'ample, 90 d'alt els valors inicials són zero.
   * Per defecte aquest marcador es posiciona verticalment dalt de la pantalla i horitzontalment enmig
   * @param app referència al PApplet principal
   */
  public Marcador (PApplet app) {
    this.app              = app;
    this.ample            = 200;
    this.alt              = 90;
    this.currentTime      = 0;
    this.puntsB           = 0;
    this.puntsA           = 0;
    this.currentCountDown = 0;
    this.score            = 0;
    this.lblPuntsA        = "HP";
    this.lblPuntsB        = "SHIELD";
    this.lblExtra         = "BOOSTER";
    this.alarmes          = new JSONArray();
    this.position         = new PVector((app.width/2 - (this.ample/2)), 10);
  }

  /**
   * Crea un marcador per defecte però se li poden modificar els textos de les etiquetes per defecte
   * @param app referència al PApplet principal
   * @param strA etiqueta que de contrari seria HOME
   * @param strB etiqueta que de contrari seria VISIT
   */
  public Marcador (PApplet app, String strA, String strB) {
    this(app);
    this.lblPuntsA = strA;
    this.lblPuntsB = strB;
  }

  /**
   * Mostra el marcador al lloc que se li indica.
   * El marcador es mostra en forma de taula com s'indica a l'inici del fitxer.
   */
  public void mostra () {
    // Obtenir dinàmicament dades del jugador a través de la instància de RescatProxima si és possible
    RescatProxima game = (RescatProxima) this.app;
    if (game != null && game.jugador != null) {
      this.puntsA = game.jugador.getVida();
      this.puntsB = game.jugador.getEscut();
      // El temps del booster en NauPlayer és en frames. El convertim a milisegons (a 60 FPS, 1 frame = ~16.6ms)
      this.currentCountDown = (int)(game.jugador.getTempsDobleDispar() * (1000.0f / 60.0f));
      
      // Obtenir etiquetes de l'idioma
      this.lblPuntsA = game.getTraduccio("vida");
      this.lblPuntsB = game.getTraduccio("escut");
      this.lblExtra  = game.getTraduccio("booster");
    }

    app.pushStyle();
    app.pushMatrix();
      app.translate(this.position.x, this.position.y);

      // Fons translúcid blau espaiat premium amb contorn cian
      app.fill(10, 15, 30, 200);
      app.stroke(0, 255, 255, 180);
      app.strokeWeight(2);
      app.rect(0, 0, this.ample, this.alt+25, 8); // Cantonades lleugerament arrodonides per estil premium

      // Dibuixar el temps de la partida al capçal
      app.textSize(18);
      app.fill(255);
      app.textAlign(PApplet.CENTER, PApplet.CENTER);
      app.text(this.getTime(), this.ample/2, 16);

      // Línia divisòria sota el temps
      app.stroke(0, 255, 255, 100);
      app.line(0, 30, this.ample, 30);

      // Etiquetes estructurals de la taula
      app.textSize(12);
      app.fill(0, 255, 255);
      app.text(lblPuntsA, this.ample/4, 42);
      app.text(lblPuntsB, 3*this.ample/4, 42);

      // Línies estructurals
      app.line(0, 50, this.ample, 50);
      app.line(this.ample/2, 30, this.ample/2, 70);

      // Dibuixar valor HP (puntsA) amb color dinàmic segons salut
      if (this.puntsA > 50) {
        app.fill(0, 255, 128); // Verd neó
      } else if (this.puntsA > 25) {
        app.fill(255, 200, 0); // Groc
      } else {
        app.fill(255, 50, 50); // Roig
      }
      app.textSize(15);
      app.text(this.puntsA, this.ample/4, 62);

      // Dibuixar valor SHIELD (puntsB) en blau elèctric / cian
      app.fill(0, 220, 255);
      app.text(this.puntsB, 3*this.ample/4, 62);

      // Línies estructurals inferiors de la taula
      app.stroke(0, 255, 255, 100);
      app.line(0, 70, this.ample, 70);
      app.line(0, 70, 0, 90);
      app.line(this.ample, 70, this.ample, 90);
      app.line(0, 90, this.ample, 90);

      // Etiqueta BOOSTER (Extra) a l'esquerra
      app.textSize(11);
      app.fill(0, 255, 255);
      app.text(this.lblExtra, this.ample/4, 82);

      // Dibuixar valor del compte enrere (Booster doble dispar)
      String strBooster = "OFF";
      if (this.currentCountDown > 0) {
        app.fill(255, 215, 0); // Daurat si està actiu
        strBooster = this.currentCountDown/1000 + ":" + app.nf(this.currentCountDown%1000, 3, 0);
      } else {
        app.fill(150); // Gris si està inactiu
      }
      app.text(strBooster, 3*this.ample/4, 82);

      // Línia per sobre del score
      app.line(0, 95, this.ample, 95);

      // Puntuació Score centrada a sota
      app.textSize(14);
      app.fill(255);
      app.text("SCORE: " + this.score, this.ample/2, 107);

    app.popMatrix();
    app.popStyle();
  }

  /**
   * Sobrecàrrega del mètode mostra per mantenir la signatura original del main loop
   * @param vidaJugador vida del jugador
   */
  public void mostra(int vidaJugador) {
    this.mostra();
  }

  /**
   * Actualitza els valors de temps del marcador usant els milisegons del sistema
   */
  public void actualitza () {
    this.currentTime = app.millis();
    
    if (this.currentCountDown > 0) {      
      this.currentCountDown -= (this.currentTime - this.countDownFlag);
      this.countDownFlag = this.currentTime;
    }
    
    if (this.currentCountDown < 0)
      this.currentCountDown = 0;
  }

  /**
   * Actualitza els valors de temps en base als frames transcorreguts de nivell
   * @param framesNivell frames acumulats en el nivell actual
   */
  public void actualitza (int framesNivell) {
    this.currentTime = (int)((framesNivell / 60.0f) * 1000);
  }

  /**
   * Aquest marcador disposa d'un sistema d'alarmes. En principi una alarma és un Objecte JSON amb
   * els següents camps: Tag, Inici, Durada, Repetir.
   * Tag: és l'etiqueta que servirà per identificar l'alarma i el seu tipus
   * Durada: temps en milisegons que ha de passar fins que salte l'alarma
   * Repetir: booleà que ens diu si l'alarma es repeteix periòdicament (true) o només una vegada (false)
   */
  public void afegirAlarma (String text, int durada, boolean esRepeteix) {
    JSONObject alarm = new JSONObject();
    alarm.setString  ("Tag", text);
    alarm.setInt     ("Inici", this.currentTime);
    alarm.setInt     ("Durada", durada);
    alarm.setBoolean ("Repetir", esRepeteix);
    this.alarmes.setJSONObject(this.alarmes.size(), alarm);
  }

  /**
   * Comprova l'array d'alarmes que s'han anat afegint al nostre marcador i 
   * torna aquelles per a les quals ja s'ha complit el temps i han saltat.
   * Si l'alarma no és de repetició, quan salta s'elimina del conjunt d'alarmes del marcador.
   * @return llista amb les alarmes que s'han disparat des de l'última volta que s'ha comprovat.
   */
  public ArrayList<String> obtenirAlarmesDisparades () {
    ArrayList <String> alAlarmes = new ArrayList<String>();
    for (int i=0; i < this.alarmes.size(); i ++) {
      JSONObject alarm = this.alarmes.getJSONObject(i);
      String etiqueta = alarm.getString("Tag");
      int    inici    = alarm.getInt("Inici");
      int    durada   = alarm.getInt("Durada");
      
      if (this.currentTime >= inici + durada) {
        alarm.setInt("Inici", this.currentTime);
        alAlarmes.add(etiqueta);
        
        if (!alarm.getBoolean("Repetir")) 
          this.alarmes.remove(i);
      }
    }
    return alAlarmes;
  }

  /******************************** GETS & SETS ********************************/
  public void sumaPuntsA   ()           { this.puntsA ++;                 }
  public void sumaPuntsB   ()           { this.puntsB ++;                 }
  public int  getPuntsA    ()           { return this.puntsA;             }
  public int  getPuntsB    ()           { return this.puntsB;             }
  public void setLblExtra  (String lbl) { this.lblExtra = lbl;            }
  
  public void setCountDown (int millis) { 
    this.currentCountDown = millis;
    this.countDownFlag    = this.currentTime;
  }

  public int getScore() {
    return this.score;
  }

  public void resetScore() {
    this.score = 0;
  }

  /**
   * Incrementa el marcador en 'inr' punts
   */
  public void incrementScore (int inr) {
    this.score += inr;
  }

  /**
   * Torna el temps en format MM:SS
   */
  public String getTime () {
    int seconds = (this.currentTime/1000)%60;
    int minutes = (this.currentTime/1000)/60;
    return app.nf(minutes,2,0) + ":" + app.nf(seconds,2,0);
  }
}
