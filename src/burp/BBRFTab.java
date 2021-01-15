package burp;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.net.URL;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;

public class BBRFTab extends JPanel {

	private BurpExtender burp;
	private JTextField textField_1;
	protected JTextField textField_2;
	
	/**
	 * Create the panel.
	 */
	public BBRFTab(BurpExtender burp) {
		
		this.burp = burp;
		
		JLabel lblBbrfProgram = new JLabel("BBRF program");
		
		JCheckBox chckbxParseDomains = new JCheckBox("Parse domains from HTTP response");
		chckbxParseDomains.setSelected(true);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				burp.program_name = textField_1.getText();
				
				burp.saveSettings();

			}
		});
		
		textField_1 = new JTextField(burp.program_name);
		textField_1.setColumns(10);
		
		JTextPane textPane = new JTextPane();
		
		JButton btnLoad = new JButton("Verify");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String result = burp.bbrf("scope in -p "+textField_1.getText());
				textPane.setText(result);
			}
		});
		
		
		JLabel lblBbrfClientLocation = new JLabel("BBRF client location");
		
		textField_2 = new JTextField(burp.bbrf_py);
		textField_2.setColumns(10);
		
		JButton btnTest = new JButton("Test");
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				burp.testBBRFClient();
			}
		});
		
		JButton btnCopyScope = new JButton("Copy scope");
		btnCopyScope.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String in_scope = burp.bbrf("scope in -p "+textField_1.getText());
				String out_scope = burp.bbrf("scope out -p "+textField_1.getText());
				
				burp.generateScope(in_scope.split("\n"), out_scope.split("\n"));
				//textPane.setText(result);
			}
		});
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(46)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblBbrfClientLocation)
								.addComponent(lblBbrfProgram))
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 318, GroupLayout.PREFERRED_SIZE)
									.addGap(3)
									.addComponent(btnLoad)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnCopyScope))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, 315, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnTest))))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(45)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(chckbxParseDomains, GroupLayout.PREFERRED_SIZE, 342, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnSave)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(45)
							.addComponent(textPane, GroupLayout.PREFERRED_SIZE, 615, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(27, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(61)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(3)
							.addComponent(lblBbrfClientLocation))
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(btnTest)))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(4)
							.addComponent(lblBbrfProgram))
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnLoad)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(btnCopyScope)))
					.addGap(26)
					.addComponent(chckbxParseDomains)
					.addGap(28)
					.addComponent(btnSave)
					.addGap(18)
					.addComponent(textPane, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
					.addContainerGap())
		);
		setLayout(groupLayout);

	}
}
