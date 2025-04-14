import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.border.LineBorder;

public class UIManagement{

    private static JFrame frame=new JFrame();
    private static JLabel titleLabel=new JLabel("Legend of Splenda: Bread of the Wild", SwingConstants.CENTER);
    private static JLabel subtitleLabel=new JLabel("A roguelike RPG by Jack Schulte", SwingConstants.CENTER);
    private static JTextArea descArea=new JTextArea();
    private static JTextArea asciiArtArea=new JTextArea();
    private static JPanel asciiPanel=new JPanel();
    private static JLabel commentLabel=new JLabel("", SwingConstants.CENTER);
    private static JPanel buttonPanel=new JPanel();
    private static Font titleFont=new Font("Trebuchet MS", Font.BOLD, 24);
    private static Font subtitleFont=new Font("Trebuchet MS", Font.BOLD, 20);
    private static Font descFont=new Font("Trebuchet MS", Font.PLAIN, 18);
    private static Font asciiLeftFont=new Font("Monospaced", Font.PLAIN, 8);
    private static Font asciiRightFont=new Font("Monospaced", Font.PLAIN, 14);
    private static Font commentFont=new Font("Trebuchet MS", Font.ITALIC, 18);
    private static Font buttonFont=new Font("Aharoni", Font.BOLD, 20);
    private static Font buttonFontHover=new Font("Aharoni", Font.BOLD, 22);
    private static GridBagConstraints gbc=new GridBagConstraints();
    private static RPG rpg=new RPG();

    private static String[] characterButtons=new String[]{"Knight", "#48AD48", "#58D558", "Sentinel", "#48AD48", "#58D558", "Assassin", "#48AD48", "#58D558", "Caveman", "#48AD48", "#58D558", "Done", "#EECC44", "#F7DD77"};
    private static int[] characterButtonLengths=new int[]{140, 140, 140, 140, 100};

    public static void main(String[] args) {

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 450);
        frame.setLayout(new BorderLayout()); // Main Layout

        // Title (at the top)
        titleLabel.setOpaque(true);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.decode("#B6220E"));
        titleLabel.setBackground(Color.decode("#EEEEEE"));

        // Stacked textPanel (for subtitle, description, ASCII art, comment)
        JPanel textPanel=new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(Box.createVerticalStrut(20)); // Adds 20 pixels of space before subtitle

        // Subtitle
        subtitleLabel.setOpaque(true);
        subtitleLabel.setFont(subtitleFont);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(subtitleLabel);
        textPanel.add(Box.createVerticalStrut(10)); // Adds 10 pixels of space before descArea

        // Description
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(descFont);
        descArea.setBackground(Color.decode("#EEEEEE"));
        JScrollPane descScroll=new JScrollPane(descArea); // Scrollable in case of long text
        descScroll.setBorder(null);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        textPanel.add(descScroll);
        textPanel.add(Box.createVerticalStrut(5)); // Adds 5 pixels of space before asciiArtArea

        // ASCII Art
        asciiArtArea.setEditable(false);
        asciiArtArea.setFont(asciiLeftFont);
        JScrollPane asciiScroll=new JScrollPane(asciiArtArea);
        asciiScroll.setBorder(null);
        
        // ASCII Panel
        asciiPanel.setLayout(new GridLayout(1, 2)); // Two columns
        asciiPanel.add(asciiScroll);
        textPanel.add(Box.createVerticalStrut(5)); // Adds 5 pixels of space before commentLabel

        // Comment
        commentLabel.setFont(commentFont);
        textPanel.add(commentLabel);

        // Buttons (at the bottom)
        buttonPanel.setBackground(Color.decode("#EEEEEE"));
        buttonPanel.setLayout(new GridBagLayout());
        gbc.insets=new Insets(5, 10, 5, 10);
        gbc.gridy=0;

        // Finalities
        frame.add(titleLabel, BorderLayout.NORTH);
        frame.add(textPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
        titleScreen();
        rpg.buildMap();
        rpg.addEventsToRooms();
        rpg.printMap();
    }


// ==============================================================================================
// =================== Screen Methods ============================================================

    private static void titleScreen(){
        String[] titleButtons={"Start!", "#EECC44", "#F7DD77"};
        int[] titleButtonLengths={120};
        makeButtons(titleButtons, titleButtonLengths);
    }
    
    private static void numPlayersScreen(){
        titleLabel.setText("");
        subtitleLabel.setText("Choose the number of players:");
        String[] numPlayersButtons={"1 Player", "#48AD48", "#58D558", "2 Players", "#48AD48", "#58D558"};
        int[] numPlayersButtonLengths={140, 140};
        makeButtons(numPlayersButtons, numPlayersButtonLengths);
    }
    
    private static void characterCreatorScreen(){
        titleLabel.setText("Player "+(RPG.getPlayerIndex()+1)+", Create your Character");
        subtitleLabel.setText("");
        makeButtons(characterButtons, characterButtonLengths);
    }

// ==============================================================================================
// =================== Action Listeners =========================================================
    
    private static void pickActionListener(JButton button, String buttonLabel){
        switch(buttonLabel){
            case "Start!":
                button.addActionListener(new ActionListener(){ // titleScreen > numPlayersScreen
                    @Override
                    public void actionPerformed(ActionEvent e){
                        buttonPanel.removeAll();
                        numPlayersScreen();
                    }
                });
                break;
            case "1 Player":
                button.addActionListener(new ActionListener(){ // numPlayersScreen > characterCreatorScreen
                    @Override
                    public void actionPerformed(ActionEvent e){
                        RPG.setNumPlayers(1);
                        RPG.createPlayers();
                        buttonPanel.removeAll();
                        characterCreatorScreen();
                    }
                });
                break;
            case "2 Players":
                button.addActionListener(new ActionListener(){ // numPlayersScreen > characterCreatorScreen
                    @Override
                    public void actionPerformed(ActionEvent e){
                        RPG.setNumPlayers(2);
                        RPG.createPlayers();
                        buttonPanel.removeAll();
                        characterCreatorScreen();
                    }
                });
                break;
            case "Knight":
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Update current button's appearance
                        button.setBackground(Color.decode("#58D58D"));
                        button.setBorder(new LineBorder(Color.BLACK, 5));
                    }
                });
                break;
            case "Sentinel":
                button.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        buttonPanel.removeAll();
                        frame.repaint();
                        frame.revalidate();
                        makeButtons(characterButtons, characterButtonLengths);
                    }
                });
                break;
            case "Assassin":
                button.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        buttonPanel.removeAll();
                        frame.repaint();
                        frame.revalidate();
                        makeButtons(characterButtons, characterButtonLengths);
                    }
                });
                break;
            case "Caveman":
                button.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        buttonPanel.removeAll();
                        frame.repaint();
                        frame.revalidate();
                        makeButtons(characterButtons, characterButtonLengths);
                    }
                });
                break;
            case "Done":
                break;
            case "Go forward":
                switch(rpg.getWayFacing()){
                    case 0:
                        //rpg.navigate(-1, 0);
                        rpg.setWayFacing(0);
                        break;
                    case 1:
                        //rpg.navigate(+1, 0);
                        rpg.setWayFacing(1);
                        break;
                    case 2:
                        //rpg.navigate(0, -1);
                        rpg.setWayFacing(2);
                        break;
                    case 3:
                        //rpg.navigate(0, +1);
                        rpg.setWayFacing(3);
                        break;
                }
                break;
            case "Go back":
                switch(rpg.getWayFacing()){
                    case 0:
                        //rpg.navigate(+1, 0);
                        rpg.setWayFacing(1);
                        break;
                    case 1:
                        //rpg.navigate(-1, 0);
                        rpg.setWayFacing(0);
                        break;
                    case 2:
                        //rpg.navigate(0, +1);
                        rpg.setWayFacing(3);
                        break;
                    case 3:
                        //rpg.navigate(0, -1);
                        rpg.setWayFacing(2);
                        break;
                }
                break;
            case "Go left":
                switch(rpg.getWayFacing()){
                    case 0:
                        //rpg.navigate(0, -1);
                        rpg.setWayFacing(2);
                        break;
                    case 1:
                        //rpg.navigate(0, +1);
                        rpg.setWayFacing(3);
                        break;
                    case 2:
                        //rpg.navigate(+1, 0);
                        rpg.setWayFacing(1);
                        break;
                    case 3:
                        //rpg.navigate(-1, 0);
                        rpg.setWayFacing(0);
                        break;
                }
                break;
            case "Go right":
                switch(rpg.getWayFacing()){
                    case 0:
                        //rpg.navigate(0, +1);
                        rpg.setWayFacing(3);
                        break;
                    case 1:
                        //rpg.navigate(0, -1);
                        rpg.setWayFacing(2);
                        break;
                    case 2:
                        //rpg.navigate(-1, 0);
                        rpg.setWayFacing(0);
                        break;
                    case 3:
                        //rpg.navigate(+1, 0);
                        rpg.setWayFacing(1);
                        break;
                }
                break;
        }
    }

// ==============================================================================================
// =================== Mouse Listeners ==========================================================
    
    private static boolean pickUniqueMouseListener(JButton button, String buttonLabel, int length, String color1, String color2){
        switch(buttonLabel){
            case "Knight":
                button.addMouseListener(new MouseAdapter(){    
                    @Override
                    public void mouseEntered(MouseEvent e){
                        button.setPreferredSize(new Dimension((int)(length*1.1), 55));
                        button.setBackground(Color.decode(color2));
                        button.setFont(buttonFontHover);
                        buttonPanel.revalidate();
                        buttonPanel.repaint();
                    /*
                        asciiArtLeft.setText("                ,^.                             ."+"\n              | |                             ."+"\n              | |                             ."+"\n              |.|                             ."+"\n              |.|                             ."+"\n              |:|      __                     ."+"\n            ,_|:|_,   /  )                    ."+"\n              (Oo    / _I_                    ."+"\n               +\\ \\  || __|                   ."+"\n                  \\ \\||___|__                 ."+"\n                    \\ /.:.  \\-\\___            ."+"\n                     |.:.   /-----\\           ."+"\n                     |_____|::oOo::|          ."+"\n                     /=[:]=|:<_T_>:|          ."+"\n                    |_____  \\ ::: /           ."+"\n                     | |  \\  \\\\:/             ."+"\n                     | |   | |                ."+"\n                     | |   | |                ."+"\n                     \\ /   | \\__              ."+"\n                     / |   \\____\\             ."+"\n                     `-'                      .");
                    */
                        subtitleLabel.setText("Knight");
                        commentLabel.setText("A strong, reliable warrior that can weather most challenges");
                    }
                    @Override
                    public void mouseExited(MouseEvent e){
                        button.setPreferredSize(new Dimension(length, 50));
                        button.setBackground(Color.decode(color1));
                        button.setFont(buttonFont);
                        buttonPanel.revalidate();
                        buttonPanel.repaint();
                        
                        subtitleLabel.setText("");
                        commentLabel.setText("");
                    }
                });
                return true;
            case "Sentinel":
                // button.addMouseListener(new MouseAdapter(){
                return true;
            case "Assassin":
                // button.addMouseListener(new MouseAdapter(){
                return true;
            case "Caveman":
                // button.addMouseListener(new MouseAdapter(){
                return true;
            case "Done":
                // button.addMouseListener(new MouseAdapter(){
                return true;
        }
        return false;
    }

// ==============================================================================================
// =================== Other Methods ============================================================

    private static void makeButtons(String[] stringArray, int[] intArray){
        for(int i=0; i<stringArray.length/3; i++){
            JButton button=new JButton(stringArray[i*3]);
            button.setPreferredSize(new Dimension(intArray[i], 50));
            button.setBackground(Color.decode(stringArray[i*3+1]));
            button.setFont(buttonFont);
            if(!pickUniqueMouseListener(button, stringArray[i*3], intArray[i], stringArray[i*3+1], stringArray[i*3+2]))
                hoverAdapter(button, intArray[i], stringArray[i*3+1], stringArray[i*3+2]);
            pickActionListener(button, stringArray[i*3]);
            gbc.gridx=i;
            buttonPanel.add(button, gbc);
        }
    }

    private static void hoverAdapter(JButton button, int length, String color1, String color2){
        button.addMouseListener(new MouseAdapter(){    
            @Override
            public void mouseEntered(MouseEvent e){
                button.setPreferredSize(new Dimension((int)(length*1.1), 55));
                button.setBackground(Color.decode(color2));
                button.setFont(buttonFontHover);
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e){
                button.setPreferredSize(new Dimension(length, 50));
                button.setBackground(Color.decode(color1));
                button.setFont(buttonFont);
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }
        });
    }

    private static void makeMoveButtons(){
        ArrayList<String> mButtons=new ArrayList<>();
        switch(rpg.getWayFacing()){
            case 0:
                if(rpg.isValidRoom(rpg.getCurrentPos()[0], rpg.getCurrentPos()[1]-1, false)) mButtons.add("Go left"); // Can go left
                if(rpg.isValidRoom(rpg.getCurrentPos()[0]-1, rpg.getCurrentPos()[1], false)) mButtons.add("Go forward"); // Can go forward
                if(rpg.isValidRoom(rpg.getCurrentPos()[0], rpg.getCurrentPos()[1]+1, false)) mButtons.add("Go right"); // Can go right
                if(rpg.isValidRoom(rpg.getCurrentPos()[0]+1, rpg.getCurrentPos()[1], false)) mButtons.add("Go back"); // Can go backward
                break;
            case 1:
                if(rpg.isValidRoom(rpg.getCurrentPos()[0], rpg.getCurrentPos()[1]+1, false)) mButtons.add("Go left"); // Can go left
                if(rpg.isValidRoom(rpg.getCurrentPos()[0]+1, rpg.getCurrentPos()[1], false)) mButtons.add("Go forward"); // Can go forward
                if(rpg.isValidRoom(rpg.getCurrentPos()[0], rpg.getCurrentPos()[1]-1, false)) mButtons.add("Go right"); // Can go right
                if(rpg.isValidRoom(rpg.getCurrentPos()[0]-1, rpg.getCurrentPos()[1], false)) mButtons.add("Go back"); // Can go backward
                break;
            case 2:
                if(rpg.isValidRoom(rpg.getCurrentPos()[0]+1, rpg.getCurrentPos()[1], false)) mButtons.add("Go left"); // Can go left
                if(rpg.isValidRoom(rpg.getCurrentPos()[0], rpg.getCurrentPos()[1]+1, false)) mButtons.add("Go forward"); // Can go forward
                if(rpg.isValidRoom(rpg.getCurrentPos()[0]-1, rpg.getCurrentPos()[1], false)) mButtons.add("Go right"); // Can go right
                if(rpg.isValidRoom(rpg.getCurrentPos()[0], rpg.getCurrentPos()[1]-1, false)) mButtons.add("Go back"); // Can go backward
                break;
            case 3:
                if(rpg.isValidRoom(rpg.getCurrentPos()[0]-1, rpg.getCurrentPos()[1], false)) mButtons.add("Go left"); // Can go left
                if(rpg.isValidRoom(rpg.getCurrentPos()[0], rpg.getCurrentPos()[1]-1, false)) mButtons.add("Go forward"); // Can go forward
                if(rpg.isValidRoom(rpg.getCurrentPos()[0]+1, rpg.getCurrentPos()[1], false)) mButtons.add("Go right"); // Can go right
                if(rpg.isValidRoom(rpg.getCurrentPos()[0], rpg.getCurrentPos()[1]+1, false)) mButtons.add("Go back"); // Can go backward
                break;
        }
        for(int i=0; i<mButtons.size(); i++){
            mButtons.add(i*3+1, "#48AD48");
            mButtons.add(i*3+2, "#58D558");
        }
        String[] moveButtons=mButtons.toArray(new String[mButtons.size()]);
        int[] moveButtonsLengths={140, 140};
        
        makeButtons(moveButtons, moveButtonsLengths);
    }


}

