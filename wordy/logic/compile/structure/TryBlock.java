package wordy.logic.compile.structure;

import wordy.logic.compile.Token;

public class TryBlock extends StatementBlock{

  public TryBlock(Token trySignifier) {
    super(BlockType.TRY);
    blockSig = trySignifier;
  }

}
