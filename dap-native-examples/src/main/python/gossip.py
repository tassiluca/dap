from dsl import dap

test_id = dap.Id(1, 2)
print(test_id.c_struct)
print(test_id.c_struct.x, test_id.c_struct.y)

test_mset = dap.MSet("Id", [dap.Id(1, 2), dap.Id(3, 4)])
print(test_mset.c_struct)
print(test_mset.c_struct.size)
print(test_mset.c_struct.elements[0])
print(test_mset.c_struct.elements[0].x, test_mset.c_struct.elements[0].y)
print(test_mset.c_struct.elements[1].x, test_mset.c_struct.elements[1].y)
