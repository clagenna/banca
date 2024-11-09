package sm.clagenna.banca.sql;

import java.sql.Types;

public enum ESqlFiltri {
  Dtmov(1, Types.DATE), //
  Dtval(2, Types.DATE), //
  Dare(4, Types.DECIMAL), //
  Avere(8, Types.DECIMAL), //
  Descr(16, Types.VARCHAR), //
  ABICaus(32, Types.VARCHAR), //
  Cardid(64, Types.VARCHAR), //
  AllSets(255, Types.NULL);

  private int flag;
  private int sqlType;

  private ESqlFiltri(int v, int sqlt) {
    flag = v;
    sqlType = sqlt;
  }

  public int getFlag() {
    return flag;
  }

  public int getSqlType() {
    return sqlType;
  }

  public boolean isSet(int v) {
    return (v & flag) != 0;
  }

  public String SQLFilter() {
    return String.format(" AND %s = ?", this.name());
  }

}
