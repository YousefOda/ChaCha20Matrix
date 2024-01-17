import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class Frame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	//encryption object for methods
	ChaCha20Matrix enc = new ChaCha20Matrix();
	
	//label to display successful encryption/decryption
	JLabel Output;
	
	//buttons
	JButton encrypt;
	JButton decrypt;
	
	//user typeable text
	JTextArea text;

	Frame() throws IOException
	{
		encrypt = new JButton("Encrypt");
		encrypt.setBounds(10, 292, 275, 97);
		encrypt.addActionListener(this);
		
		decrypt = new JButton("Decrypt");
		decrypt.setBounds(290, 292, 275, 97);
		decrypt.addActionListener(this);
		
		//text field
		text = new JTextArea("Enter text you would like to encrypt/Enter the name of the file you would like to decrypt");
		text.setBounds(10, 10, 556, 200);
		
		//creates the UI
		Output = new JLabel("");
		Output.setForeground(Color.black);
		Output.setFont(new Font("Bitstream Vera Sans", 0, 20));
		Output.setBounds(10, 200, 468, 100);
		
		JFrame frame = new JFrame();
		frame.setTitle("Encryptor/Decryptor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setSize(586, 450);
		frame.setVisible(true);
		frame.add(encrypt);
		frame.add(decrypt);
		frame.add(text);
		frame.add(Output);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == encrypt)
		{
			try 
			{
				enc.encrypt(text.getText());
				Output.setText("Successfully encrypted the file! Check project folder.");
			} 
			
			catch (IOException e1) 
			{
				Output.setText("ERROR");
			}
		}
		
		if(e.getSource() == decrypt)
		{
			try 
			{
				enc.decrypt(text.getText());
				Output.setText("Successfully decrypted the file! Check project folder.");
			} 
			
			catch (IOException e1) 
			{
				Output.setText("File not found");
			}
		}
	}
}