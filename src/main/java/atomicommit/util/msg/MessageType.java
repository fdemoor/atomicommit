package atomicommit.util.msg;

public enum MessageType {

  /* Transaction messages */
  TR_XACT,
  TR_COMMIT,
  TR_ABORT,
  TR_YES,
  TR_NO,
  TR_START,
  TR_ACK,
  TR_HELP,
  TR_CONS_COMMIT,
  TR_CONS_ABORT,

  /* Consensus messages */
  CONS_START,
  CONS_XACT,
  CONS_YES,
  CONS_NO,
  CONS_COMMIT,
  CONS_ABORT,
  CONS_ACK

}
