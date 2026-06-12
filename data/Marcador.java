/************************************************************/
/*                                                          */
/*                Formatted Current Time                    */
/*                                                          */
/************************************************************/
/*                        score                             */
/************************************************************/
/*        lblPuntsA         *        lblPuntsB              */
/************************************************************/
/*          puntsA          *          puntsB               */
/************************************************************/
/*        lblExtra                Formatted CountDown       */
/************************************************************/
/** 
 * La classe marcador és la classe que s'encarregara de portar el control del temps i mostrar-lo.
 * Aquesta classe disposarà de 2 comptadors A i B al quals se li assignarà una etiqueta: lblPuntsA o lblPuntsB
 * i un valor puntsA o puntsB que es motraràn a la pantalla principal de l'aplicació.
 * Els objectes de la classe marcador també disposaran d'un conjunt d'alarmes (Nom-Interaval) per que avise
 * a l'aplicació principal del pas de cert interval de temps, per exemple. Per tal d'implementar aquestes alarmes
 * utilitzarem un JSONArray encara que també podriem dissenyar la nostra classe Alarma amb tots els membres necessaris.
 */
public class Marcador {
  private PVector   position;          /** Posició en la que es dibuixarà el Marcador */
  private int       ample;             /** Ample en píxels del marcadors */
  private int       alt;               /** Alt en píxels del marcador */
  private int       currentTime;       /** Temps en milisegons actual (transcorregut des de la creació de l'objecte marcador) */
  private int       currentCountDown;  /** Temps de compte enrere en cas de vole utilitzar-se. Aquest atribut sol combinar-se amb countDownFlag */
  private int       countDownFlag;     /** Per controlar quan comencen i acaben els comptes enrere necessite un flag o inici del compte Enrere */
  private int       score;             /** Punts totals en cas d'haver-ne */
  private String    lblPuntsA;         /** Etiqueta que identifica el valor del primer marcador (ex. LOCAL) */
  private String    lblPuntsB;         /** Etiqueta que identifica el valor del segon marcador (ex. VISITANT) */
  private String    lblExtra;          /** Etiqueta extra. Podria identificar el temps extra **/
  private int       puntsB;            /** Comptador del primer marcador */
  private int       puntsA;            /** Comptador del segon marcador */
  private JSONArray alarmes;           /** Col·lecció amb les alarmes establides al marcador */
  /**
  * Crea un marcador per defecte de 200 píxels d'ample, 90 d'alt els valors inicials són zero i a les etiquetes A i B posa HOME i VISIT
  * Per defecte aquest marcador es posiciona verticalment dalt de la pantalla i horitzontalment enmig 
  */
  public Marcador () {
    this.ample            = 200;
    this.alt              = 90;
    this.currentTime      = 0;
    this.puntsB           = 0;
    this.currentCountDown = 0;
    this.score            = 0;
    this.lblPuntsA        = "HOME";
    this.lblPuntsB        = "VISIT";
    this.lblExtra         = "EXTRA";
    this.alarmes          = new JSONArray();
    this.position         = new PVector((width/2-(this.ample/2)), 10);
  }
  /**
  * Crea un marcador per defecte però se li poden modificar els textos de les etiquetes per defecte
  * @param strA etiqueta que de contrari seria HOME
  * @param strB etiqueta que de contrari seria VISIT
  */
  public Marcador (String strA, String strB) {
    this();
    this.lblPuntsA = strA;
    this.lblPuntsB = strB;
  }
  /**
  * Mostra el marcador al lloc que se li indica. 
  * El marcador es mostra en forma de taula com s'indica a l'inici del fitxer
  */
  public void mostra () {
    pushMatrix();
      translate(this.position.x,this.position.y);
      fill(color(#98FB98));
      stroke(0);
      strokeWeight(2);
      rect(0, 0, this.ample, this.alt+25);
      textSize(30);
      fill(0);
      textAlign(CENTER);
      text(this.getTime(),this.ample/2,25);
      textSize(16);
      line(0,30,this.ample,30);
      text(lblPuntsA,this.ample/4,45);
      text(lblPuntsB,3*this.ample/4,45);
      line(0,50,this.ample,50);
      line(this.ample/2,30,this.ample/2,70);
      textSize(18);
      text(this.puntsA, this.ample/4,66);
      text(this.puntsB, 3*this.ample/4,66);
      line(0,70,this.ample,70);
      line(0,70,0,90);
      line(this.ample,70,this.ample,90);
      line(0,90,this.ample,90);
      textSize(16);
      text(this.lblExtra,this.ample/4,86);
      String strBooster = this.currentCountDown/1000 + ":" + nf(this.currentCountDown%1000,3,0);
      text(strBooster, 3*this.ample/4,86);
      textSize(18);
      line(0, 115, this.ample, 115);
      text(this.score, this.ample/2, 108);
    popMatrix();
  }
  /**
  * Actualitza els valors de temps del marcador
  */
  public void actualitza () {
    this.currentTime = millis();
    
    if (this.currentCountDown > 0) {      
      this.currentCountDown -= (this.currentTime - this.countDownFlag); //<>//
      this.countDownFlag = this.currentTime;
    }
    
    if (this.currentCountDown < 0)
      this.currentCountDown = 0;
  }
  /**
  * Aquest marcador disposa d'un sistema d'alarmes. En principi una alarma és un Objecte JSON amb
  * els següents camps: Tag, Inici, Durada, Repetir.
  * Tag: és l'etiqueta que servirà per identificar l'alarmaa i el seu tipus
  * Durada: temps en milisegons que ha de passar fins que salte l'alarma
  * Repetir: booleà que ens diu si l'alarma es repeteix periòdicament (true) o només una vegada (false)
  */
  public void afegirAlarma (String text, int durada, boolean esRepeteix) {
    JSONObject alarm = new JSONObject(); //<>//
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
    return nf(minutes,2,0) + ":" + nf(seconds,2,0);
  }
}
