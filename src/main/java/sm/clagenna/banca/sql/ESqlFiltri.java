package sm.clagenna.banca.sql;

import java.sql.Types;

public enum ESqlFiltri {
  Id(1, Types.DATE), //
  tipo(2, Types.VARCHAR), //
  Dtmov(4, Types.DATE), //
  Dtval(8, Types.DATE), //
  Dare(16, Types.DECIMAL), //
  Avere(32, Types.DECIMAL), //
  Descr(64, Types.VARCHAR), //
  ABICaus(128, Types.VARCHAR), //
  Cardid(256, Types.VARCHAR), //
  AllSets(511, Types.NULL);

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
