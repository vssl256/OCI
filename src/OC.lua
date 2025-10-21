local file = io.open( "out.txt", "r" )
if not file then print("Error during file reading") return end

local content = file:read("*a")

repeat
    local char = file:read(3)
    if not char then break end
    print ("color: "..string.byte(char))


until false
print("stopped")
file:close()

