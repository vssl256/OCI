local internet = require("internet")
local component = require("component")
local coroutine = require("coroutine")
local os = require("os")
local keyboard = require("keyboard")

local gpu = component.gpu

local ip = "helx.ddns.net"
local url = "http://"..ip.."/file"
local handle
local prevHandle
local bytes = {}
local w, h = 160, 50

function getImage()
    local ok
    ok, handle = pcall(internet.request, url)
    if not ok or not handle then
        print("Ошибка запроса:", handle)
        return nil
    end

    local data = ""
    for chunk in handle do
        data = data .. chunk
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

function clear()
    gpu.setBackground(0x000000)
    gpu.setForeground(0xFFFFFF)
    gpu.fill(1, 1, w, h, " ")
end

function drawImage()
    local index = 1

    w = bytes[index]; index = index + 1
    h = bytes[index]; index = index + 1

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

            local r = bytes[index]
            local g = bytes[index + 1]
            local b = bytes[index + 2]

            index = index + 3

            local color = (r << 16) | (g << 8) | b
            gpu.setBackground(color)
            gpu.set(x, y, " ")
        end
    end
end

function setDefaultPalette()
    c = 15
    for i = 0, 15 do
        color = (c << 16) | (c << 8) | c
        gpu.setPaletteColor(i, color)
        c = c + 15
    end
end

clear()
while true do
    local updated = getImage()
    if updated then
        clear()
        drawImage()
    end
    os.sleep(0.5)
    if keyboard.isControlDown() then
        setDefaultPalette()
        clear()
        os.exit()
    end
end