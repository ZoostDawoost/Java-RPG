import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RPG{
    
    public int[][] map=new int[21][21];
    private int rooms=1;
    private ArrayList<int[]> roomList=new ArrayList<>();
    private ArrayList<int[]> middleMapRoomList=new ArrayList<>();
    private Random ran=new Random();
    private int[] currentPos;
    private int wayFacing; // 0 = up, 1 = down, 2 = left, 3 = right
    private int difficulty;

    // Player stats
    private int playerNum;
    private String name;
    private String nameClass;
    private static int numPlayers=0;
    private static int playerIndex=0;
    private int vig=0;
    private int def=0;
    private int str=0;
    private int dex=0;
    private int agt=0;
    private int luck=0;
    private int hp=0;
    private InvSlot invSlot1;
    private InvSlot invSlot2;
    private InvSlot invSlot3;
    private InvSlot invSlot4;

    private static ArrayList<RPG> players = new ArrayList<>();
    private static RPG knight=new RPG("", "Knight", 11, 12, 11, 10, 8, 8, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
    private static RPG sentinel=new RPG("", "Sentinel", 10, 13, 9, 11, 7, 10, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
    private static RPG assassin=new RPG("", "Assassin", 9, 8, 11, 12, 13, 7, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);
    private static RPG caveman=new RPG("", "Caveman", 10, 10, 10, 10, 10, 10, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty);

    public RPG(){
    }
    
    public RPG(String name, String nameClass, int vig, int def, int str, int dex, int agt, int luck, InvSlot invSlot1, InvSlot invSlot2, InvSlot invSlot3, InvSlot invSlot4){
        this.name=name;
        this.nameClass=nameClass;
        this.vig=vig;
        this.def=def;
        this.str=str;
        this.dex=dex;
        this.agt=agt;
        this.luck=luck;
        this.invSlot1=invSlot1;
        this.invSlot2=invSlot2;
        this.invSlot3=invSlot3;
        this.invSlot4=invSlot4;
    }

    public int getStr(){
        return str;
    }

    public int getDex(){
        return dex;
    }
    
    public int[] getCurrentPos(){
        return currentPos;
    }
    
    public void setCurrentPos(int[] x){
        currentPos=x;
    }
    
    public int getWayFacing(){
        return wayFacing;
    }

    public void setWayFacing(int x){
        wayFacing=x;
    }
    
    public int getDifficulty(){
        return difficulty;
    }
    
    public void setDifficulty(int x){
        difficulty=x;
    }
    
    public static int getNumPlayers(){
        return numPlayers;
    }
    
    public static void setNumPlayers(int x){
        numPlayers=x;
    }

    public static int getPlayerIndex(){
        return playerIndex;
    }
    
    public static void modPlayerIndex(){
        playerIndex++;
    }
    
    public static void createPlayers() {
        players.clear(); // Clears the existing Players list if any
        for (int i=0; i<numPlayers; i++) {
            players.add(new RPG("", "", 0, 0, 0, 0, 0, 0, InvSlot.empty, InvSlot.empty, InvSlot.empty, InvSlot.empty));
        }
    }

    // public static void classAssignment()
    
    public void buildMap(){
        // Sets starting position
        map[10][10]=2;
        roomList.add(new int[]{10, 10});

        while (rooms<50){
            Collections.shuffle(roomList, ran);  // Randomizes where to expand
            int[] room=roomList.get(0);
            int row=room[0];
            int col=room[1];
            ArrayList<int[]> possibleMoves=new ArrayList<>();

            // Checks four directions for valid room expansion
            if (isValidRoom(row-1, col, true)) possibleMoves.add(new int[]{row-1, col});
            if (isValidRoom(row+1, col, true)) possibleMoves.add(new int[]{row+1, col});
            if (isValidRoom(row, col-1, true)) possibleMoves.add(new int[]{row, col-1});
            if (isValidRoom(row, col+1, true)) possibleMoves.add(new int[]{row, col+1});
            // Adds duplicate option to prioritize moving in a straight line
            switch(roomLineHelper(row, col)){
                case 0:
                    possibleMoves.add(new int[]{row+1, col});
                    break;
                case 1:
                    possibleMoves.add(new int[]{row-1, col});
                    break;
                case 2:
                    possibleMoves.add(new int[]{row, col+1});
                    break;
                case 3:
                    possibleMoves.add(new int[]{row, col-1});
                    break;
            }
            
            if (!possibleMoves.isEmpty()){
                // Randomly selects one of the valid directions
                int[] newRoom=possibleMoves.get(ran.nextInt(possibleMoves.size()));
                int newRow=newRoom[0];
                int newCol=newRoom[1];

                // Places the new room
                map[newRow][newCol]=1;
                rooms++;
                // Adds this new room to the appropriate list(s)
                roomList.add(newRoom);
                if(newRow+newCol>=18||newRow+newCol<=22)
                    middleMapRoomList.add(newRoom);
            }
        }
    }
    
    public boolean isValidRoom(int row, int col, boolean isBuilding){
        if(isBuilding){ // Used when building the map
            if(row<1 || row>=20 || col<1 || col>=20) // Is in map boundaries?
                return false;
            if(map[row][col]!=0) // Is the space empty?
                return false;
            return !formsSquare(row, col); // Makes a 2x2 cluster?
        } else{ // Used when navigating the map
            if(row<1 || row>=20 || col<1 || col>=20)
                return false;
            if(map[row][col]!=0)
                return true;
            return false;
        }
    }

    private boolean formsSquare(int row, int col){
        // Checks all possible 2x2 formations
        return (map[row - 1][col] == 1 && map[row][col - 1] == 1 && map[row - 1][col - 1] == 1) ||
               (map[row - 1][col] == 1 && map[row][col + 1] == 1 && map[row - 1][col + 1] == 1) ||
               (map[row + 1][col] == 1 && map[row][col - 1] == 1 && map[row + 1][col - 1] == 1) ||
               (map[row + 1][col] == 1 && map[row][col + 1] == 1 && map[row + 1][col + 1] == 1);
    }

    private int roomLineHelper(int row, int col){
        if(map[row-1][col]!=0&&isValidRoom(row+1, col, true)) return 0; // Go down
        if(map[row+1][col]!=0&&isValidRoom(row-1, col, true)) return 1; // Go up
        if(map[row][col-1]!=0&&isValidRoom(row, col+1, true)) return 2; // Go right
        if(map[row][col+1]!=0&&isValidRoom(row, col-1, true)) return 3; // Go left
        return -1; // No valid moves
    }
    
    public void addEventsToRooms(){
        int shopRooms=0;
        int shrineRooms=0;
        int bossRooms=0;
        for(int i=0; i<map.length; i++){
            for(int j=0; j<map[i].length; j++){
                if(map[i][j]==1){
                    int event=ran.nextInt(100)+1; // Random number between 1 and 100
                    if(event<=25) map[i][j]=3; // Standard enemy room (25% chance)
                    else if(event<=40) map[i][j]=4; // Difficult enemy room (15% chance)
                    else if(event<=55){ // Market room (15% chance)
                        map[i][j]=5; 
                        shopRooms++;
                    }
                    else if(event<=65){ // Blacksmith room (10% chance)
                        map[i][j]=6; 
                        shopRooms++;
                    }
                    else if(event<=75) map[i][j]=7; // Treasure room (10% chance)
                    else if(event<=90) map[i][j]=8; // Plain room (15% chance)
                    else if(event<=100&&i+j>22&&i+j<18) map[i][j]=9; // Obstacle room (10% chance)
                    else map[i][j]=3; // Standard enemy room
                }
            }
        }
        while(bossRooms<4){ // Creates 3 Boss rooms randomly from roomList
            Collections.shuffle(roomList, ran);
            int[] randomRoom=roomList.get(0);
            if(randomRoom[0]+randomRoom[1]>22&&randomRoom[0]+randomRoom[1]<18){ // Avoids placing a Boss room in the middle of the map
                map[randomRoom[0]][randomRoom[1]]=2;
                bossRooms++;
            }
        }
        while(shrineRooms<4){ // Creates 3 Shrine rooms randomly from roomList
            if(shrineRooms==0){ // Ensures at least one is in the middle of the map
                Collections.shuffle(middleMapRoomList, ran);
                int[] randomRoom=roomList.get(0);
                if(map[randomRoom[0]][randomRoom[1]]!=2){ // Avoids starting room
                    map[randomRoom[0]][randomRoom[1]]=5;
                    shrineRooms++;
            }
            }
            Collections.shuffle(roomList, ran);
            int[] randomRoom=roomList.get(0);
            if(map[randomRoom[0]][randomRoom[1]]!=2){ // Avoids starting room and Boss rooms
                map[randomRoom[0]][randomRoom[1]]=5;
                shrineRooms++;
            }
        }
        while(shopRooms<3){ // Ensures at least 3 Shop rooms exist
            Collections.shuffle(roomList, ran);
            int[] randomRoom=roomList.get(0);
            if(map[randomRoom[0]][randomRoom[1]]!=2&&map[randomRoom[0]][randomRoom[1]]!=5){ // Avoids starting room and Boss rooms and Shrine rooms
                map[randomRoom[0]][randomRoom[1]]=4;
                shopRooms++;
            }
        }
    }

    public void printMap(){
        for(int i=0; i<map.length; i++){
            for(int j=0; j<map[i].length; j++){
                System.out.print(map[i][j] + " ");
            }
            System.out.println(); // New line after each row
        }
    }
    
    public String showHealth() {
        StringBuilder healthBar = new StringBuilder();
        for (int i = 0; i < hp; i++) {
            healthBar.append("█");
        }
        return healthBar.append(" ").append(hp).toString();
    }

}
