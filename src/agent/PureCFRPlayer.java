package agent;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import abstraction.GameAbstraction;
import acpc.Action;
import acpc.MatchState;


public class PureCFRPlayer{

    public PureCFRPlayer() {}
    
    public static void main(String args[]) {
        // Print usage
        if (args.length != 4) {
            System.out.println("Usage: pure_cfr_player <player_file> <server> <port>\n");
        }
        
        /* Initialize player module and get the abstract game */
        File file = new File(args[0]);
        PlayerModule playerModule = new PlayerModule(file);
        GameAbstraction gameAbs = playerModule.gameAbs;
        
        /* Connect to the dealer */
        int port;
        try {
            Socket s = new Socket(args[1], Integer.parseInt(args[2]));
            try {
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
                if (in == null || out == null) {
                    System.out.println("ERROR: could not get socket streams");
                    return ;
                }
                //TODO
                
                PrintWriter pw = new PrintWriter(out);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                
                // Send version string to dealer
                pw.write("VERSION:" + 2 + "." + 0 + "." + 0 + "\n");
                pw.flush();
            
                // play the game 
                String serverStr = null;
                while ((serverStr = br.readLine()) != null) {
                	System.out.println(serverStr);
                    if(serverStr.charAt(0) == '#' || serverStr.charAt(0) == ';') {
                        continue;
                    }
                    
                    // Read the incoming state
                    MatchState matchState = new MatchState();
                    matchState.readMatchState(gameAbs.game, serverStr);
                    
                    // Ignore game over message
                    if (matchState.finished) {
                        continue;
                    }
                    
                    // Ignore states that we are not acting in
                    int currentPlayer = gameAbs.game.currentPlayer(matchState);
                    if (currentPlayer  != matchState.viewingPlayer ) {
                        continue;
                    }
                    
                    /* Start building the response to the server by adding a colon
                     * (guaranteed to fit because we read a new-line in fgets)
                     */
                    StringBuilder serverSb = new StringBuilder(serverStr);
                    serverSb.append(":");
                    
                    // Get action to the server
                    Action action = playerModule.getAction(matchState);
                    
                    // Send the action to the server
                    serverSb.append(action.toString());
                    serverSb.append("\r\n");
                    pw.write(serverSb.toString());
                    pw.flush();
                }   
            }catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Socket error");
            e.printStackTrace();
        }
            
        
        
    }
    
}