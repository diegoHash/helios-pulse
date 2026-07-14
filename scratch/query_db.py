import sys
import subprocess
import os

try:
    import pymysql
except ImportError:
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pymysql"])
    import pymysql

try:
    required = ["HELIOS_PULSE_DDBB_HOST", "HELIOS_PULSE_DDBB_USERNAME", "HELIOS_PULSE_DDBB_PASS", "HELIOS_PULSE_DDBB_NAME"]
    missing = [name for name in required if not os.environ.get(name)]
    if missing:
        raise RuntimeError("Missing required environment variables: " + ", ".join(missing))

    conn = pymysql.connect(
        host=os.environ["HELIOS_PULSE_DDBB_HOST"],
        user=os.environ["HELIOS_PULSE_DDBB_USERNAME"],
        password=os.environ["HELIOS_PULSE_DDBB_PASS"],
        database=os.environ["HELIOS_PULSE_DDBB_NAME"],
        cursorclass=pymysql.cursors.DictCursor
    )
    with conn.cursor() as cursor:
        print("--- Querying all V2 transactions on June 15, 2026 ---")
        cursor.execute("SELECT * FROM etecc_cbt_transaction_v2 WHERE delivery_date LIKE '2026-06-15%'")
        txs = cursor.fetchall()
        for t in txs:
            print(t)
            cursor.execute(f"SELECT * FROM etecc_cbt_payment_v2 WHERE transaction_id = {t['id']}")
            print("Payments:", cursor.fetchall())

except Exception as e:
    print("Error:", e)
finally:
    if 'conn' in locals() and conn:
        conn.close()
