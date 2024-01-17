import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class ChaCha20Matrix
{
    private final long maxUnsignedInt = 
        Long.divideUnsigned(Long.MAX_VALUE, 
                            Long.valueOf((long)Math.pow(2, 31))) - 1;
    
    // 4294967295 which is max unsigned int in 32 bits
	
	public ChaCha20Matrix()
	{
		
	}

    private void printArray(Integer arr[][])
    {
        for(int i = 0; i < 4; ++i)
        {
            for(int j = 0; j < 3; ++j)
            {
                System.out.print(Integer.toUnsignedLong(arr[i][j]) + ", ");
            }

            System.out.print(Integer.toUnsignedLong(arr[i][3]) + "\n");
        }

        System.out.println();
    }

    private Integer[][] matrixMultiplication(final Integer arr1[][], 
                                             final Integer arr2[][])
    {
        Integer matMult[][] = {{0, 0, 0, 0},
                               {0, 0, 0, 0},
                               {0, 0, 0, 0},
                               {0, 0, 0, 0}};
        
        // The last time I programmed matrix multiplication in Java was in
        // Grade 12 Computer Science. Thankfully I took linear algebra last
        // year!

        // Also, I learned that there is an unsolved problem in computer science
        // regarding finding the fastest algorithm for matrix multiplication,
        // wow!

        // Recall matrix multiplication (since we have square matrices, this is
        // simpler)
        // C = A x B = A[b1 b2 ... bn] = [Ab1 Ab2 ... Abn], that is, it is
        // matrix-vector multiplication!
        
        // Matrix-vector multiplication:
        // Ax = [a11 a12 a13 a14][x1] = [a11x1 + a12x2 + a13x3 + a14x4]
        //      [a21 a22 a23 a24][x2]   [a21x1 + a22x2 + a23x3 + a24x4]
        //      [a31 a32 a33 a34][x3]   [a31x1 + a32x2 + a33x3 + a34x4]
        //      [a41 a42 a43 a44][x4]   [a41x1 + a42x2 + a43x3 + a44x4]
        
        for(int i = 0; i < 4; ++i)
        {
            for(int j = 0; j < 4; ++j)
            {
                for(int k = 0; k < 4; ++k)
                {
                    matMult[i][j] = matMult[i][j] + (arr1[i][k] * arr2[k][j]);
                }
            }
        }

        return matMult;
    }

    private Integer[][] transpose(final Integer S[][])
    {
        Integer t[][] = new Integer[4][4];

        for(int i = 0; i < 4; ++i)
        {
            for(int j = 0; j < 4; ++j)
            {
                t[j][i] = S[i][j];
            }
        }

        return t;
    }

    private String PRBG(final Integer S[][], final int numOfKeyStreams)
    {
        String keystream = "";
        // Here is how this will work:

        // We will actually take S' = SC = S, SC = S^T (transpose), 
        // do SC = S' x SC  (matrix multiplication), 10 times, and 
        // finally, return the keystream SC ^ S' (xor)
        // We then do this again for as many keystreams as we need
        // S' is never changed
        for(int i = 0; i < numOfKeyStreams; ++i)
        {
            Integer SP[][] = S;
            Integer SC[][] = S;

            // Set c in SC before starting
            SC[3][0] = i;

            for(int j = 0; j < 10; j++)
            {
                // System.out.println("SC before transpose: ");
                // printArray(SC);
                SC = transpose(SC);
                // System.out.println("SC after transpose: ");
                // printArray(SC);

                // We then do matrix multiplication
                // System.out.println("SC before multiplication: ");
                // printArray(SC);
                SC = matrixMultiplication(SC, SP);
                // System.out.println("SC after multiplication: ");
                // printArray(SC);
            }

            // Bitwise xor with SP
            // No need to convert just yet!
            Integer XOR[][] = new Integer[4][4];

            for(int j = 0; j < 4; ++j)
            {
                for(int k = 0; k < 4; ++k)
                {
                    XOR[j][k] = SC[j][k] ^ SP[j][k];
                }
            }

            // Convert XOR into a string to get our keystream (every 16 bits
            // corresponds to a single char)
            // We convert each one into an Integer, take the binary
            // representation (will be 32 bits), and each 16 will be for one
            // character, which will be converted into a char, and added to the
            // keystream string
            for(int j = 0; j < 4; ++j)
            {
                for(int k = 0; k < 4; ++k)
                {
                    String bin = Integer.toBinaryString(XOR[j][k]);

                    // Check that length is 32, if not, add 0's to front 
                    // until it is
                    while(bin.length() < 32)
                    {
                        bin = "0" + bin;
                    }

                    // Split the bits up in half
                    String s1 = bin.substring(0, 17);
                    String s2 = bin.substring(16, 32);

                    // We then convert these into their char representations
                    char c1 = (char)((int)Integer.parseInt(s1, 2));
                    char c2 = (char)((int)Integer.parseInt(s2, 2));

                    // Add to keystream
                    keystream += "" + c1;
                    keystream += "" + c2;
                }
            }
        }

        return keystream;
    }
	
	public void encrypt(String message) throws IOException
	{
		// This will essentially follow the same format as the standard 
        // ChaCha20 Algorithm made by Dan Bernstein, but instead of using the
        // quarter round function, I have opted to do matrix multiplication!
        // NOTE! THIS MAY OR MAY NOT MAKE THIS VARIATION OF THE ChaCha20
        // ENCRYPTION SCHEME MORE SECURE!

        // First, we need to format the input string so it is a multiple of
        // 64 (each keystream is 64 bytes, each character is 2 bytes (b/c 
        // Java uses UTF-16 encoding), so we need to have multiples of 32 chars)

        // Pad out with '\0' if needed
        System.out.println("Value of maxUnsignedInt: " + maxUnsignedInt);
        System.out.println("Padding message");
        while((message.length() % 32) != 0)
        {
            message += '\0';
        }

        // We then can begin the scheme
        // Recall how the scheme works:

        // We have a 256-bit key k, 96-bit nonce n, 128-bit constant f, and
        // 32-bit counter c. The initial state S is (in a 4x4 matrix):
        //
        // [[f_1, f_2, f_3, f_4],
        //  [k_1, k_2, k_3, k_4],
        //  [k_5, k_6, k_7, k_8],
        //  [c  , n_1, n_2, n_3]]

        // Note that f, n, and c may be public, but the key must
        // NEVER be public!!!

        // Note that a nonce is a non-repeating quantity, that is, it is
        // NEVER reused when encrypting! So how do 2 people know which nonce
        // to not use? One strategy is to have A use nonces starting
        // with 1 and B use nonces starting with 0 (but there is still 
        // and issue with this??)
        // However, for our purposes, we will randomly generate the nonce, so
        // it is possible that it will repeat! (WHICH IS VERY BAD!)

        // Select a nonce n and set counter c = 0
        // While we need our keystream bytes:
        //      S' = S
        //      Apply function to S (10 times for quarter round)
        //      Return S ^ S'
        //      Increment c

        // SINCE WE ARE DOING MATRIX MULTIPLICATION, IT HELPS THAT NONE OF THE
        // 32-bit VALUES ARE 0! OTHERWISE, IT WOULDN'T BE VERY HELPFUL :)

        // RECALL ALSO! A SQUARE MATRIX IS INVERTIBLE <==> DETERMINANT != 0
        // So, to some extent (depending on the initial state), this may
        // not be invertible, which is somewhat good!

        // So, we first set up our initial state as a 4x4 matrix
        // Constants and key never change, so we get them from a file
        // If a file does not exist, we will make one for them!
        Integer S[][] = new Integer[4][4];
        
        // File where info is stored
        String file = "values.txt";
        
        System.out.println("Reading from file if it exists");
        try
        {
            // Try to find the file in the project folder
            Scanner sc = new Scanner(new File(file));

            System.out.println("File exists!");
            // At this point, we have found the file!
            // It will be formatted where each 32-bit word is on their own
            // line, with the constant first (4 words), then the key (8 words)

            // Keys
            for(int i = 0; i < 4; ++i)
            {
                S[1][i] = (int)(long)sc.nextLong();
            }

            for(int i = 0; i < 4; ++i)
            {
                S[2][i] = (int)(long)sc.nextLong();
            }

            // Constant
            for(int i = 0; i < 4; ++i)
            {
                S[0][i] = (int)(long)sc.nextLong();
            }

            // Close file scanner
            System.out.println("Done reading file!");
            sc.close();
        }

        catch(Throwable fnfe)
        {
            System.out.println("File does not exist, creating file");
            // Make the file and choose random non-zero constants and key!
            Random rand = new Random();

            // Write values into the file
            FileWriter fw = new FileWriter(file);
	        PrintWriter pw = new PrintWriter(fw);
            
            // First the key (which we will also put in the initial state)
            for(int i = 0; i < 4; ++i)
            {
                Long k = rand.nextLong(1, maxUnsignedInt);
                S[1][i] = k.intValue();
                pw.println(Integer.toUnsignedString(k.intValue()));
            }

            for(int i = 0; i < 4; ++i)
            {
                Long k = rand.nextLong(1, maxUnsignedInt);
                S[2][i] = k.intValue();
                pw.println(Integer.toUnsignedString(k.intValue()));
            }

            // And we do the constant
            for(int i = 0; i < 4; ++i)
            {
                Long f = rand.nextLong(1, maxUnsignedInt);
                S[0][i] = f.intValue();
                pw.println(Integer.toUnsignedString(f.intValue()));
            }
            
            // Close the print and file writers
            System.out.println("Done creating file!");
            pw.close();
            fw.close();
        }

        // Now we need our nonce n which we randomly choose
        System.out.println("Creating nonce");
        for(int i = 1; i < 4; ++i)
        {
            Random rand = new Random();

            Long n = rand.nextLong(1, maxUnsignedInt);
            S[3][i] = n.intValue();
        }

        // The last is the counter which will be set when we give S
        // to the PRBG
        // We will set it to 0 here just to be safe
        S[3][0] = 0;
        
        System.out.println("Value of S before PRGB:");
        printArray(S);

        // We now call the PRBG to get the keystream
        int numOfKeyStreams = message.length() / 32;
        String keystream = PRBG(S, numOfKeyStreams);
        
        // We then xor the keystream with the message to get the cipher text
        // We have to xor each character...
        // Print out the ciphertext to a file as we go
        FileWriter fw = new FileWriter("Encrypted.txt");
	    PrintWriter pw = new PrintWriter(fw);

        // Attach the nonce first!
        for(int i = 1; i < 4; ++i)
        {
            pw.println(S[3][i]);
        }

        for(int i = 0; i < message.length(); ++i)
        {
            char a = (char)(message.charAt(i) ^ keystream.charAt(i));
            System.out.println("message XOR keystream:");
            System.out.println("message: " + Integer.toBinaryString(message.charAt(i)));
            System.out.println("keystream: " + Integer.toBinaryString(keystream.charAt(i)));
            System.out.println("ciphertext: " + Integer.toBinaryString(a));
            System.out.println();

            pw.println(Integer.valueOf(a));
        }

        pw.close();
        fw.close();

        System.out.println("Done!");
	}

    public void decrypt(String filename) throws IOException
    {
        // Decrypting is easy, read in the file for the nonce, read in the
        // values.txt file for the constant and key, run them through the
        // PRBG, and xor it with the ciphertext

        Integer S[][] = new Integer[4][4];

        // Get the key
        System.out.println("Getting key and constants");
        Scanner sc = new Scanner(new File("values.txt"));

        for(int i = 0; i < 4; ++i)
        {
            S[1][i] = (int)(long)sc.nextLong();
        }

        for(int i = 0; i < 4; ++i)
        {
            S[2][i] = (int)(long)sc.nextLong();
        }

        // And the constant
        for(int i = 0; i < 4; ++i)
        {
            S[0][i] = (int)(long)sc.nextLong();
        }

        sc.close();

        // Get the nonce from the encrypted file
        System.out.println("Getting nonce");
        sc = new Scanner(new File(filename));

        for(int i = 1; i < 4; ++i)
        {
            S[3][i] = (int)(long)sc.nextLong();
        }

        // Set c = 0
        S[3][0] = 0;

        // Read in the rest of the ciphertext into an arrayList
        System.out.println("Getting ciphertext");
        ArrayList<Character> ciphertext = new ArrayList<Character>();
        
        while(sc.hasNext())
        {
            ciphertext.add((char)sc.nextInt());
        }

        sc.close();

        // Get the keystream
        System.out.println("Getting keystream");
        int numOfKeyStreams = ciphertext.size() / 32;
        String keystream = PRBG(S, numOfKeyStreams);

        // XOR the keystream with the ciphertext
        String message = "";

        for(int i = 0; i < ciphertext.size(); ++i)
        {
            char c = (char)(ciphertext.get(i) ^ keystream.charAt(i));
            message += c;
        }

        // Minor bug: When padding it out from encrypting, the NULLS appear,
        // so we need to erase them
        int index = message.length() - 1;
        
        while(message.charAt(index) == '\0')
        {
            --index;
        }

        if(index != message.length() - 1)
        {
            message = message.substring(0, index + 1);
        }

        // Print out message to file
        FileWriter fw = new FileWriter("Decrypted.txt");
		PrintWriter pw = new PrintWriter(fw);

        pw.println(message);

        pw.close();
        fw.close();

        System.out.println("Done!");
    }

}
