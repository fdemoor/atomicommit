package atomicommit.util.msg;

/** Message types */
public enum MessageType {

  /* Transaction messages */
  TR_XACT,
  TR_PREPARE,
  TR_COMMIT,
  TR_ABORT,
  TR_YES,
  TR_NO,
  TR_START,
  TR_ACK,
  TR_HELP,
  TR_HELPED,
  TR_COLL,
  TR_CONS_COMMIT,
  TR_CONS_ABORT,

  /* Consensus messages */
  CONS_START,
  CONS_XACT,
  CONS_YES,
  CONS_NO,
  CONS_COMMIT,
  CONS_ABORT,
  CONS_ACK,

  NODE_STOP

}
