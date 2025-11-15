local internet = require("internet")
local component = require("component")
local os = require("os")
local keyboard = require("keyboard")
local io = require("io")
local unicode = require("unicode")
local term = require("term")

local gpu = component.gpu

local ip = "helx.ddns.net"
local baseurl = "http://"..ip.."/file?"
local url = baseurl
local currentChunk = 1
local handle
local prevHandle
local bytes = {}
local w, h = 135, 50

local primaryScreen = component.screen.address
local screenTable = component.list("screen")
local sortedTable = {}

local userInput

function clear()
    gpu.setBackground(0x000000)
    gpu.setForeground(0xFFFFFF)
    gpu.fill(1, 1, w, h, " ")
end

function initScreens()
    function hint()
        for screen, _ in pairs(screenTable) do
            if next(component.proxy(screen).getKeyboards()) then mainScreen = screen end
            if (screen == mainScreen) then goto skip end

            gpu.bind(screen, true)
            clear()
            gpu.setResolution(4, 1)
            gpu.set(1, 1, screen)
            ::skip::
        end
        gpu.bind(mainScreen, true)
        gpu.setResolution(80, 25)
    end

    function read()
        local file = io.open("screens.txt", "r")
        if not file then
            write()
            file = io.open("screens.txt", "r")
        end

        for line in file:lines() do
            table.insert(sortedTable, line)
        end
    end

    function write()
        local file = io.open("screens.txt", "w")
        local i = 0
        while true do
            local line = io.read()
            local screen = component.get(line)
            if not screen then print("Screen not found\n") goto continue else i = i + 1 end
            if line ~= "" then
                file:write(screen.."\n")
            else
                file:close()
                return
            end
            print("Screens: "..i)
            ::continue::
        end
    end

    hint()
    read()
end

function getImage()
    local ok
    ok, handle = pcall(internet.request, url..".bin")
    if not ok or not handle then
        print("Ошибка запроса:", handle)
        return nil
    end

    local data = ""

    local success, err = pcall(function()
        for chunk in handle do
            data = data .. chunk
        end
    end)

    if not success then
        print("Ошибка запроса: "..err)
        return nil
    end

    handle = nil

    if prevHandle == data then
        return false
    end

    prevHandle = data

    bytes = {}
    for i = 1, #data do
        bytes[#bytes + 1] = string.byte(data, i)
    end

    return true
end

function drawTwoPixels(x, y, upperColor, lowerColor)
    gpu.setForeground(upperColor)
    gpu.setBackground(lowerColor)
    gpu.set(x, y, unicode.char(tonumber("281B", 16)))
end

function drawImage()
    for i, screen in ipairs(sortedTable) do
        local index = 1
        url = baseurl..userInput.."/output/"..i
        getImage()

        gpu.bind(mainScreen,false)
        print("Drawing "..i)

        gpu.bind(screen, true)
        clear()
        gpu.setResolution(135, 50)

        wHigh = bytes[index]; index = index + 1
        wLow = bytes[index]; index = index + 1
        w = ( wHigh << 8 ) | wLow

        hHight = bytes[index] / 2; index = index + 1
        hLow = bytes[index] / 2; index = index + 1
        h = ( hLow << 8 ) | hLow
        h = h / 2

        for i = 0, 15 do
            local r = bytes[index]
            local g = bytes[index + 1]
            local b = bytes[index + 2]
            index = index + 3
            local color = (r << 16) | (g << 8) | b
            gpu.setPaletteColor(i, color)
        end

        for y = 1, h do
            for x = 1, w do
                if index + 2 > #bytes then break end

                local ur = bytes[index]
                local ug = bytes[index + 1]
                local ub = bytes[index + 2]

                index = index + 3

                local lr = bytes[index]
                local lg = bytes[index + 1]
                local lb = bytes[index + 2]

                index = index + 3

                local upperColor = (ur << 16) | (ug << 8) | ub
                local lowerColor = (lr << 16) | (lg << 8) | lb

                drawTwoPixels(x, y, upperColor, lowerColor)
            end
        end
    end
    gpu.bind(mainScreen, false)
    gpu.setResolution(80, 25)
end

function setDefaultPalette()
    c = 15
    for i = 0, 15 do
        color = (c << 16) | (c << 8) | c
        gpu.setPaletteColor(i, color)
        c = c + 15
    end
end

function clearAllScreens()
    for i, screen in ipairs(sortedTable) do
        gpu.bind(screen, false)
        gpu.setResolution(135, 50)
        clear()
        setDefaultPalette()
    end
    gpu.bind(mainScreen, false)
    gpu.setResolution(80, 25)
end

function start()
    clear()
    term.setCursor(1, 1)
    print("Enter image name:")
    local name, screenWidth, screenHeight = query()
    userInput = "width="..screenWidth.."&height="..screenHeight.."&name="..name
    url = baseurl..userInput.."/output/"..currentChunk
    local updated = getImage()
    if updated then drawImage() end
    while true do
        --local updated = getImage()
        --if updated then
        --    drawImage()
        --end
        os.sleep(0.5)
        if keyboard.isControlDown() then
            name, screenWidth, screenHeight = query()
            userInput = "width="..screenWidth.."&height="..screenHeight.."&name="..name
            if string.find(userInput, "exit") then
                clearAllScreens()
                os.exit()
            end
            if name ~= nil then url = baseurl..userInput.."/output/"..1 drawImage() end
        end
    end
end

function query()
    local name, screenWidth, screenHeight = io.read("*line"):match("^(%S+)%s*(%d*)%s*(%d*)$")
    screenWidth = tonumber(screenWidth) or 1
    screenHeight = tonumber(screenHeight) or 1
    return name, screenWidth, screenHeight
end

initScreens()
start()
