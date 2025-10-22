local file = io.open( "test.bin", "rb" )
if not file then print("Error during file reading") return end

repeat
    local char = file:read(1)
    if not char then break end
    print ("color: "..string.byte(char))

until false

print("stopped")
file:close()

