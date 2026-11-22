# -*- coding: utf-8 -*-
from datetime import datetime, timedelta, date
from pathlib import Path

out = Path(r"e:\table\cursorTry\meiye-store-server\src\main\resources\db\seed.sql")

def q(v):
    return "'" + str(v).replace("'", "''") + "'"

def ins(table, cols, values):
    body = ",\n  ".join("(" + ", ".join(row) + ")" for row in values)
    return f"INSERT INTO {table} ({', '.join(cols)}) VALUES\n  {body};"

services = [
    ("深层清洁", "面部护理", 168, 60),
    ("补水护理", "面部护理", 198, 60),
    ("美白焕肤", "面部护理", 298, 75),
    ("抗衰紧致", "面部护理", 398, 90),
    ("眼部护理", "面部护理", 128, 40),
    ("肩颈舒缓", "身体护理", 188, 50),
    ("全身精油SPA", "身体护理", 468, 90),
    ("背部拨筋", "身体护理", 228, 45),
    ("淋巴排毒", "身体护理", 358, 70),
    ("纤体塑形", "身体护理", 520, 80),
    ("头皮护理", "头疗", 168, 45),
    ("养发健发", "头疗", 238, 60),
    ("手部护理", "美甲美睫", 88, 30),
    ("足部护理", "美甲美睫", 128, 40),
    ("日式美甲", "美甲美睫", 168, 60),
    ("嫁接睫毛", "美甲美睫", 268, 70),
    ("小气泡", "皮肤管理", 328, 50),
    ("光子嫩肤", "皮肤管理", 680, 40),
    ("脱毛护理", "皮肤管理", 258, 40),
    ("新娘妆造", "妆造", 880, 120),
]
beauticians = [
    "陈雨桐", "周晚宁", "苏小满", "沈清禾", "顾星妍",
    "林知夏", "叶晚晴", "许念安", "蒋书宁", "韩予安",
    "唐暖暖", "宋以安", "方清越", "邓晚舟", "曹予白",
    "彭星河", "冯清和", "崔晚棠", "潘知意", "任清欢",
]
customers = [
    "赵敏", "钱芳", "孙丽", "李娜", "周婷", "吴霞", "郑洁", "王静", "冯雪", "陈琳",
    "褚倩", "卫兰", "蒋欣", "沈悦", "韩梅", "杨柳", "朱迪", "秦岚", "尤娜", "许晴",
]
levels = ["普通会员", "银卡会员", "金卡会员", "钻石会员"]
b_levels = ["初级", "中级", "高级", "首席"]
sources = ["到店", "美团", "抖音", "转介绍", "小程序"]
tags = [
    "敏感肌", "干皮", "油皮", "熟客", "新客", "高客单", "会员日", "抗衰需求", "美白需求", "肩颈劳损",
    "夜猫子", "企业客户", "闺蜜同行", "复购稳定", "价格敏感", "指定女技师", "偏好晚间", "皮肤管理", "妆造意向", "头皮稀疏",
]
addresses = [
    "西湖区文三路188号", "拱墅区湖墅南路66号", "上城区延安路258号", "滨江区江南大道88号", "余杭区文一西路520号",
    "西湖区黄龙路18号", "萧山区市心路99号", "临平区世纪大道120号", "西湖区学院路77号", "拱墅区大关路36号",
    "上城区望江路210号", "滨江区网商路599号", "余杭区五常大道300号", "西湖区天目山路398号", "钱塘区金沙大道88号",
    "西湖区西溪路150号", "拱墅区莫干山路199号", "上城区解放路88号", "滨江区长河路66号", "余杭区良睦路228号",
]
products = [
    ("卸妆油", "瓶", 12, 8),
    ("氨基酸洁面", "支", 10, 36),
    ("爽肤水", "瓶", 10, 42),
    ("修护精华", "瓶", 6, 4),
    ("保湿面霜", "瓶", 8, 28),
    ("补水面膜", "盒", 20, 15),
    ("玫瑰精油", "瓶", 8, 22),
    ("按摩膏", "罐", 6, 18),
    ("身体乳", "瓶", 10, 9),
    ("眼霜", "支", 5, 16),
    ("洗发水", "瓶", 8, 30),
    ("护发素", "瓶", 8, 24),
    ("甲油胶", "瓶", 15, 11),
    ("睫毛胶水", "支", 6, 20),
    ("脱毛蜡", "盒", 5, 7),
    ("一次性床单", "包", 30, 26),
    ("化妆棉", "包", 40, 80),
    ("卸妆棉片", "包", 20, 55),
    ("消毒酒精", "瓶", 10, 14),
    ("美容大巾", "条", 12, 6),
]
# 前 12 单已完成或已到店，记消费；后面几单覆盖待确认、取消和爽约
appt_status = (
    ["COMPLETED"] * 8
    + ["ARRIVED"] * 4
    + ["CONFIRMED"] * 3
    + ["PENDING"] * 2
    + ["CANCELLED"] * 2
    + ["NO_SHOW"]
)

parts = [
    "-- 掌美店管演示数据：顾客、美容师、项目、预约、库存，每块约 20 条，门店 ID = 1",
    "USE meiye_store;",
    "SET NAMES utf8mb4;",
    "START TRANSACTION;",
]

parts.append(ins(
    "biz_service_item",
    ["id", "store_id", "name", "category", "price", "duration_min", "status", "remark"],
    [[str(i), "1", q(n), q(c), f"{p:.2f}", str(d), "'ENABLED'", q(c + "常规项目")]
     for i, (n, c, p, d) in enumerate(services, 1)],
))

parts.append(ins(
    "biz_beautician",
    ["id", "store_id", "name", "mobile", "gender", "level", "status", "entry_date", "remark"],
    [[
        str(i), "1", q(name), q(f"1370000{i:04d}"),
        q("MALE" if i in (15, 16) else "FEMALE"),
        q(b_levels[(i - 1) % 4]),
        q("LEFT" if i == 20 else "ACTIVE"),
        q(date(2021, 1, 6) + timedelta(days=45 * (i - 1))),
        q(b_levels[(i - 1) % 4] + "美容师"),
    ] for i, name in enumerate(beauticians, 1)],
))

consumed_amount = {}
for i, status in enumerate(appt_status, 1):
    if status in ("COMPLETED", "ARRIVED"):
        consumed_amount[i] = services[i - 1][2]

parts.append(ins(
    "biz_customer",
    ["id", "store_id", "name", "mobile", "gender", "birthday", "level", "source", "address", "remark", "status", "total_amount", "visit_count"],
    [[
        str(i), "1", q(name), q(f"1360000{i:04d}"),
        q("MALE" if i in (17, 18) else "FEMALE"),
        q(date(1988, 2, 8) + timedelta(days=170 * (i - 1))),
        q(levels[(i - 1) % 4]),
        q(sources[(i - 1) % 5]),
        q("杭州市" + addresses[i - 1]),
        q(tags[i - 1]),
        q("DISABLED" if i == 19 else "ACTIVE"),
        f"{consumed_amount.get(i, 0):.2f}",
        "1" if i in consumed_amount else "0",
    ] for i, name in enumerate(customers, 1)],
))

parts.append(ins(
    "biz_customer_tag",
    ["id", "store_id", "customer_id", "tag_name"],
    [[str(i), "1", str(i), q(tags[i - 1])] for i in range(1, 21)],
))

parts.append(ins(
    "biz_beautician_service",
    ["id", "store_id", "beautician_id", "service_item_id"],
    [[str(i), "1", str(i), str(i)] for i in range(1, 21)],
))

sched = []
for i in range(1, 21):
    work = date(2026, 11, 22)
    if i == 19:
        sched.append([str(i), "1", str(i), q(work), "'LEAVE'", "NULL", "NULL", "'事假'"])
    elif i == 20:
        sched.append([str(i), "1", str(i), q(work), "'OFF'", "NULL", "NULL", "'轮休'"])
    else:
        sched.append([str(i), "1", str(i), q(work), "'ON_DUTY'", "'09:30:00'", "'21:00:00'", "'常规班'"])
parts.append(ins(
    "biz_beautician_schedule",
    ["id", "store_id", "beautician_id", "work_date", "shift_status", "shift_start", "shift_end", "remark"],
    sched,
))

appts = []
consumes = []
for i, status in enumerate(appt_status, 1):
    name, _, price, dur = services[i - 1]
    start = datetime(2026, 11, i, 10 + (i % 6), 0)
    reason = "NULL"
    if status == "CANCELLED":
        reason = q("顾客临时改期")
    elif status == "NO_SHOW":
        reason = q("未到店")
    appts.append([
        str(i), "1", str(i), str(i), str(i),
        q(start.strftime("%Y-%m-%d %H:%M:%S")),
        q(status), f"{price:.2f}", reason,
    ])
    if i in consumed_amount:
        done = start + timedelta(minutes=dur)
        consumes.append([
            str(i), "1", str(i), str(i), str(i), str(i),
            f"{price:.2f}", q(done.strftime("%Y-%m-%d %H:%M:%S")), q(name + "到店消费"),
        ])
parts.append(ins(
    "biz_appointment",
    ["id", "store_id", "customer_id", "beautician_id", "service_item_id", "appoint_time", "status", "amount", "cancel_reason"],
    appts,
))
parts.append(ins(
    "biz_consumption",
    ["id", "store_id", "customer_id", "beautician_id", "service_item_id", "appointment_id", "amount", "consumed_at", "remark"],
    consumes,
))

parts.append(ins(
    "inv_product",
    ["id", "store_id", "name", "sku_code", "unit", "warn_quantity", "status", "remark"],
    [[
        str(i), "1", q(name), q(f"SKU-{i:03d}"), q(unit), str(warn),
        q("DISABLED" if i == 20 else "ENABLED"), q("门店常备耗材"),
    ] for i, (name, unit, warn, qty) in enumerate(products, 1)],
))
parts.append(ins(
    "inv_stock",
    ["id", "store_id", "product_id", "quantity"],
    [[str(i), "1", str(i), str(qty)] for i, (*_, qty) in enumerate(products, 1)],
))
parts.append(ins(
    "inv_stock_flow",
    ["id", "store_id", "product_id", "biz_type", "change_qty", "before_qty", "after_qty", "biz_id", "remark", "created_at"],
    [[
        str(i), "1", str(i), "'IN'", str(qty), "0", str(qty), str(1000 + i),
        q(name + "期初入库"),
        q((datetime(2026, 11, 1, 9, 0) + timedelta(hours=i)).strftime("%Y-%m-%d %H:%M:%S")),
    ] for i, (name, _, _, qty) in enumerate(products, 1)],
))
takes = []
for i, (name, _, _, qty) in enumerate(products, 1):
    actual = qty if i % 2 == 0 else max(qty - 2, 0)
    takes.append([
        str(i), "1", str(i), str(qty), str(actual), str(actual - qty),
        q(name + "月中盘点"),
        q((datetime(2026, 11, 15, 18, 0) + timedelta(minutes=10 * i)).strftime("%Y-%m-%d %H:%M:%S")),
    ])
parts.append(ins(
    "inv_stocktake",
    ["id", "store_id", "product_id", "book_qty", "actual_qty", "diff_qty", "remark", "created_at"],
    takes,
))
parts.append(ins(
    "biz_service_material",
    ["id", "store_id", "service_item_id", "product_id", "quantity"],
    [[str(i), "1", str(i), str(i), "1.50" if i % 2 else "1.00"] for i in range(1, 21)],
))

parts.append("COMMIT;")
out.write_text("\n\n".join(parts) + "\n", encoding="utf-8")
print(out)
