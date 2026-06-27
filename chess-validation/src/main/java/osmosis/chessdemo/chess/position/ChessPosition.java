package osmosis.chessdemo.chess.position;

import static osmosis.chessdemo.chess.position.ChessNotationProvider.getChessNotation;

public class ChessPosition {
	private final File file;
	private final Rank rank;

	public ChessPosition(File file, Rank rank) {
		this.file = file;
		this.rank = rank;
	}

	public File getFile() {
		return file;
	}

	public Rank getRank() {
		return rank;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ChessPosition)) {
			return false;
		}
		ChessPosition position = (ChessPosition) object;
		return position.getRank().equals(getRank()) && position.getFile().equals(getFile());
	}

	@Override
	public String toString() {
		return getChessNotation(this);
	}
}
