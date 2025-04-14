public class Weapon extends InvSlot{

    private String weaponName;
    private String weaponType;
    private int cost;
    private double dropRate;
    private double critRate;
    private double strScaling;
    private double dexScaling;
    private int strReq;
    private int dexReq;
    private int dmgLoEnd;
    private int dmgRange;
    private int critDmg;
    
    public Weapon(String weaponName, String weaponType, int cost, double dropRate, double critRate, double strScaling, double dexScaling, int strReq, int dexReq, int dmgLoEnd, int dmgRange, int critDmg){
        this.weaponName=weaponName;
        this.weaponType=weaponType;
        this.cost=cost;
        this.dropRate=dropRate;
        this.critRate=critRate;
        this.strScaling=strScaling;
        this.dexScaling=dexScaling;
        this.strReq=strReq;
        this.dexReq=dexReq;
        this.dmgLoEnd=dmgLoEnd;
        this.dmgRange=dmgRange;
        this.critDmg=critDmg;
    }

    public int getCost(){
        return this.cost;
    }
    
    public double getDropRate(){
        return this.dropRate;
    }
    
    public double getCritRate(){
        return this.critRate;
    }

    public double getStrScaling(){
        return this.strScaling;
    }

    public double getDexScaling(){
        return this.dexScaling;
    }

    public int getStrReq(){
        return this.strReq;
    }

    public int getDexReq(){
        return this.dexReq;
    }

    public int getDmgLoEnd(){
        return this.dmgLoEnd;
    }

    public int getDmgRange(){
        return this.dmgRange;
    }

    public int getCritDmg(){
        return this.critDmg;
    }

    public static double doDamage(RPG player, Weapon w){
        int baseDamage=(int)(Math.random()*w.getDmgRange()+w.getDmgLoEnd());
        int critDamage=0;
        if(Math.random()<w.getCritRate())
            critDamage=w.getCritDmg();
        double scalingBonus=(w.strScaling*player.getStr())*(w.dexScaling*player.getDex());
        return baseDamage*scalingBonus+critDamage;
    }
}
