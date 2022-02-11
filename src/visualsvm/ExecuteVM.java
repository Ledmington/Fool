package visualsvm;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.*;

public class ExecuteVM {

	private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	public static final int MEMSIZE = 10000;
	public static final int CODESIZE = 10000;

	private final int[] code;
	private final int[] memory;

	private int ip = 0;
	private int sp = MEMSIZE; // punta al top dello stack

	private int tm;
	private int hp = 0;
	private int ra;
	private int fp = MEMSIZE;

	private final JFrame frame;
	private final JList<String> asmList;
	private final JList<String> stackList, heapList;
	private final JButton nextStep;
	private final JButton play;
	private final JLabel tmLabel, raLabel, fpLabel, ipLabel, spLabel, hpLabel;
	private final JScrollPane asmScroll;
	private final JTextArea outputText;

	private final int codeLineCount;
	private String keyboardCommand = "";

	private final int[] sourceMap;

	public ExecuteVM(int[] code, int[] sourceMap, List<String> source) {

		this.code = code;
		this.sourceMap = sourceMap;
		this.memory = new int[MEMSIZE];

		this.frame = new JFrame("FOOL Virtual Machine");
		JPanel mainPanel = new JPanel();

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		this.play = new JButton("PLAY");
		this.play.addActionListener(e -> this.playButtonHandler());
		this.nextStep = new JButton("STEP");
		this.nextStep.addActionListener(e -> this.stepButtonHandler());
		buttonPanel.add(this.play);
		buttonPanel.add(this.nextStep);

		JPanel registerPanel = new JPanel();
		this.tmLabel = new JLabel();
		this.tmLabel.setFont(FONT);
		this.raLabel = new JLabel();
		this.raLabel.setFont(FONT);
		this.fpLabel = new JLabel();
		this.fpLabel.setFont(FONT);
		this.ipLabel = new JLabel();
		this.ipLabel.setFont(FONT);
		this.spLabel = new JLabel();
		this.spLabel.setFont(FONT);
		this.hpLabel = new JLabel();
		this.hpLabel.setFont(FONT);
		registerPanel.setLayout(new BoxLayout(registerPanel, BoxLayout.Y_AXIS));
		registerPanel.add(this.tmLabel);
		registerPanel.add(this.raLabel);
		registerPanel.add(this.fpLabel);
		registerPanel.add(this.ipLabel);
		registerPanel.add(this.spLabel);
		registerPanel.add(this.hpLabel);

		mainPanel.setLayout(new BorderLayout());
		this.asmList = new JList<>();
//		final List<String> disassembly = new ArrayList<>();
//		int i;
//		for (i = 0; i < this.code.length && this.code[i] != 0; i++) {
//			disassembly.add(String.format("%5d: %s", i, SVMParser.tokenNames[this.code[i]].replace("'", "")));
//			if (Arrays.asList(SVMParser.PUSH, SVMParser.BRANCH, SVMParser.BRANCHEQ, SVMParser.BRANCHLESSEQ)
//					.contains(this.code[i])) {
//				i++;
//				disassembly.add(String.format("%5d: %d", i, this.code[i]));
//			}
//		}
//		this.codeLineCount = i;
//		this.asmList.setListData(new Vector<>(disassembly));
		for (int i = 0; i < source.size(); i++) {
			source.set(i, String.format("%5d: %s", i, source.get(i)));
		}
		this.asmList.setListData(new Vector<>(source));
		this.codeLineCount = source.size();


		this.asmList.setFont(FONT);
		for (MouseListener m : this.asmList.getMouseListeners()) {
			this.asmList.removeMouseListener(m);
		}
		for (MouseMotionListener m : this.asmList.getMouseMotionListeners()) {
			this.asmList.removeMouseMotionListener(m);
		}
		this.asmScroll = new JScrollPane(this.asmList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(this.asmScroll, BorderLayout.EAST);

		this.stackList = new JList<>();
		this.heapList = new JList<>();
		setMem();
		this.stackList.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
		this.heapList.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
		JScrollPane stackScroll = new JScrollPane(this.stackList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane heapScroll = new JScrollPane(this.heapList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JSplitPane memPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				stackScroll, heapScroll);
		mainPanel.add(memPanel, BorderLayout.CENTER);

		this.outputText = new JTextArea();
		this.outputText.setRows(7);
		this.outputText.setEditable(false);
		JScrollPane outputScroll = new JScrollPane(this.outputText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


		this.frame.getContentPane().setLayout(new BorderLayout());
		this.frame.add(mainPanel, BorderLayout.CENTER);
		this.frame.add(buttonPanel, BorderLayout.EAST);
		this.frame.add(registerPanel, BorderLayout.WEST);
		this.frame.add(outputScroll, BorderLayout.SOUTH);

		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.outputText.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				ExecuteVM.this.keyboardCommand += e.getKeyChar();
				ExecuteVM.this.checkKeyboardCommand();
			}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
		});

		this.update();
		this.frame.setMinimumSize(new Dimension(800, 500));
		this.frame.pack();

		stackScroll.getVerticalScrollBar().setValue(stackScroll.getVerticalScrollBar().getMaximum());
		memPanel.setDividerLocation(0.5);

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			e.printStackTrace();
			System.exit(1);
		});
	}



	private void checkKeyboardCommand() {
		if (this.keyboardCommand.endsWith(" ")) {
			this.stepButtonHandler();
		} else if (this.keyboardCommand.endsWith("\n")) {
			this.playButtonHandler();
		} else if (this.keyboardCommand.endsWith("fra")) {
			this.play.setEnabled(false);
		} else if (this.keyboardCommand.endsWith("tranqui")) {
			this.play.setEnabled(true);
		} else {
			return;
		}
		this.keyboardCommand = "";
	}



	private void setMem() {
		this.stackList.setListData(new Vector<>(
				IntStream.range(0, MEMSIZE).mapToObj(x -> String.format("%5d: %s", x, x <= hp || x >= sp ? this.memory[x] : ""))
						.collect(Collectors.toList())));
		this.heapList.setListData(new Vector<>(
				IntStream.range(0, MEMSIZE).mapToObj(x -> String.format("%5d: %s", x, x <= hp || x >= sp ? this.memory[x] : ""))
						.collect(Collectors.toList())));
	}

	private void update() {
		this.raLabel.setText("RA: " + this.ra);
		this.fpLabel.setText("FP: " + this.fp);
		this.tmLabel.setText("TM: " + this.tm);
		this.ipLabel.setText("IP: " + this.ip);
		this.hpLabel.setText("HP: " + this.hp);
		this.spLabel.setText("SP: " + this.sp);
		this.asmList.clearSelection();
		this.asmList.setSelectedIndex(this.sourceMap[this.ip]);
		final JScrollBar s = this.asmScroll.getVerticalScrollBar();
		int dest = this.sourceMap[this.ip] * s.getMaximum() / this.codeLineCount - s.getHeight() / 2;
		s.setValue(Math.max(dest, 0));
		setMem();
	}

	public void cpu() {
		this.frame.setVisible(true);
	}

	private void playButtonHandler() {
		while (this.step()) ;
		this.nextStep.setEnabled(false);
		this.play.setEnabled(false);
		this.update();
	}

	private void stepButtonHandler() {
		boolean play = this.step();
		if (!play) {
			this.nextStep.setEnabled(false);
			this.play.setEnabled(false);
		} else {
			this.update();
		}
	}

	private boolean step() {
		int bytecode = fetch();
		int v1, v2;
		int address;
		switch (bytecode) {
			case SVMParser.PUSH:
				v1 = fetch();
				push(v1);
				break;
			case SVMParser.POP:
				pop();
				break;
			case SVMParser.ADD:
				v1 = pop();
				v2 = pop();
				push(v2 + v1);
				break;
			case SVMParser.SUB:
				v1 = pop();
				v2 = pop();
				push(v2 - v1);
				break;
			case SVMParser.MULT:
				v1 = pop();
				v2 = pop();
				push(v2 * v1);
				break;
			case SVMParser.DIV:
				v1 = pop();
				v2 = pop();
				push(v2 / v1);
				break;
			case SVMParser.STOREW:
				address = pop();
				memory[address] = pop();
				break;
			case SVMParser.LOADW:
				push(memory[pop()]);
				break;
			case SVMParser.BRANCH:
				ip = fetch();
				break;
			case SVMParser.BRANCHEQ:
				address = fetch();
				v1 = pop();
				v2 = pop();
				ip = v2 == v1 ? address : ip;
				break;
			case SVMParser.BRANCHLESSEQ:
				address = fetch();
				v1 = pop();
				v2 = pop();
				ip = v2 <= v1 ? address : ip;
				break;
			case SVMParser.JS:
				address = pop();
				ra = ip;
				ip = address;
				break;
			case SVMParser.LOADRA:
				push(ra);
				break;
			case SVMParser.STORERA:
				ra = pop();
				break;
			case SVMParser.LOADTM:
				push(tm);
				break;
			case SVMParser.STORETM:
				tm = pop();
				break;
			case SVMParser.LOADFP:
				push(fp);
				break;
			case SVMParser.STOREFP:
				fp = pop();
				break;
			case SVMParser.COPYFP:
				fp = sp;
				break;
			case SVMParser.LOADHP:
				push(hp);
				break;
			case SVMParser.STOREHP:
				hp = pop();
				break;
			case SVMParser.PRINT:
				final String output = sp == MEMSIZE ? "EMPTY STACK" : Integer.toString(memory[sp]);
				System.out.println(output);
				this.outputText.append(output + "\n");
				break;
			case SVMParser.HALT:
				return false;
		}
		if (this.sp <= this.hp) {
			System.out.println("Segmentation fault");
			this.outputText.append("Segmentation fault\n");
			return false;
		}
		return true;
	}

	private int pop() {
		return memory[sp++];
	}

	private void push(int v) {
		memory[--sp] = v;
	}

	private int fetch() {
		return code[ip++];
	}}