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
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;

public class BBRFTab extends JPanel {

	private BurpExtender burp;
	private JTextField textField_1;
	
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
		
		JButton btnCopyScope = new JButton("Copy scope");
		btnCopyScope.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String in_scope = burp.bbrf("scope in -p "+textField_1.getText());
				String out_scope = burp.bbrf("scope out -p "+textField_1.getText());
				
				burp.generateScope(in_scope.split("\n"), out_scope.split("\n"));
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(45)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(textPane, GroupLayout.PREFERRED_SIZE, 615, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnSave)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblBbrfProgram)
							.addGap(43)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 318, GroupLayout.PREFERRED_SIZE)
							.addGap(3)
							.addComponent(btnLoad)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCopyScope))
						.addComponent(chckbxParseDomains, GroupLayout.PREFERRED_SIZE, 342, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(20, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(40)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(4)
							.addComponent(lblBbrfProgram))
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnLoad)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(btnCopyScope)))
					.addGap(18)
					.addComponent(chckbxParseDomains)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGap(37)
					.addComponent(btnSave)
					.addGap(18)
					.addComponent(textPane, GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
					.addGap(38))
		);
		setLayout(groupLayout);

	}
}
