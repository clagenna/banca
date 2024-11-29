package sm.clagenna.banca.sql;

import java.sql.Types;

public enum ESqlFiltri {
  Id(1, Types.DATE), //
  Dtmov(2, Types.DATE), //
  Dtval(4, Types.DATE), //
  Dare(8, Types.DECIMAL), //
  Avere(16, Types.DECIMAL), //
  Descr(32, Types.VARCHAR), //
  ABICaus(64, Types.VARCHAR), //
  Cardid(128, Types.VARCHAR), //
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
